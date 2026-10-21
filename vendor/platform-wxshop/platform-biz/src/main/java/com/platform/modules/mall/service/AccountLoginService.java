package com.platform.modules.mall.service;

import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 已验证微信身份的短事务入口, 账户变化与协议确认一起提交或回滚. */
@Service
public class AccountLoginService {
  private final JdbcTemplate jdbc;
  private final WxUserService users;
  private final LegalDocumentService documents;

  /** 微信网络交换由控制器完成, 此服务只处理身份持久化及确认记录. */
  public AccountLoginService(
      JdbcTemplate jdbc, WxUserService users, LegalDocumentService documents) {
    this.jdbc = jdbc;
    this.users = users;
    this.documents = documents;
  }

  /** 标识仅接受服务端SDK的可信结果, 不接受客户端自报OpenID或个人资料覆盖. */
  @Transactional(rollbackFor = Exception.class)
  public WxUserEntity login(
      boolean miniProgram,
      String openid,
      String unionId,
      LegalDocumentService.Acceptance acceptance) {
    String identity = identity(openid, 64, false);
    String union = identity(unionId, 128, true);
    String column = miniProgram ? "MA_OPENID" : "OPENID";
    List<String> locks = new ArrayList<>();
    locks.add(hash(column + ":" + identity));
    if (union != null) {
      locks.add(hash("UNION_ID:" + union));
    }
    // 多入口采用相同锁顺序, UnionID未提供时仍按当前小程序身份串行化.
    for (String key : locks.stream().distinct().sorted().toList()) {
      jdbc.update(
          "INSERT INTO CC_LOGIN_IDENTITY_LOCK(identity_hash) VALUES(?)"
              + " ON DUPLICATE KEY UPDATE touched_at=NOW()",
          key);
    }
    Map<String, Object> found = resolveAccount(column, identity, union, locks);
    Long userId;
    if (found == null) {
      WxUserEntity user = new WxUserEntity();
      if (miniProgram) {
        user.setMaOpenid(identity);
      } else {
        user.setOpenid(identity);
      }
      user.setUnionId(union);
      user.setNickname("微信用户");
      user.setHeadImgUrl("/static/images/logo.png");
      user.setIntegral(0);
      user.setAllIntegral(0);
      user.setRegisterTime(new Date());
      user.setLastLoginTime(new Date());
      if (!users.save(user) || user.getId() == null) {
        throw new IllegalStateException("账户创建未完成");
      }
      userId = user.getId();
    } else {
      Object oldUnion = found.get("UNION_ID");
      if (union != null && oldUnion != null && !union.equals(oldUnion)) {
        throw new IllegalArgumentException("历史账户身份不一致, 请联系商家核对");
      }
      userId = ((Number) found.get("ID")).longValue();
      jdbc.update(
          "UPDATE WX_USER SET "
              + column
              + "=COALESCE("
              + column
              + ",?),UNION_ID=COALESCE(UNION_ID,?),LAST_LOGIN_TIME=NOW() WHERE ID=?",
          identity,
          union,
          userId);
    }
    // 共享UnionID的已验证入口可以拥有不同OpenID, 保留原入口并绑定新别名.
    for (String key : locks) {
      jdbc.update("UPDATE CC_LOGIN_IDENTITY_LOCK SET user_id=? WHERE identity_hash=?", userId, key);
    }
    documents.accept(userId, acceptance);
    WxUserEntity user = users.getById(userId);
    if (user == null) {
      throw new IllegalStateException("登录账户状态发生变化");
    }
    return user;
  }

  /** 按已验证字段和入口别名解析唯一账户, 历史冲突不猜测归属. */
  private Map<String, Object> resolveAccount(
      String column, String identity, String union, List<String> locks) {
    Map<String, Object> byOpenid = find(column, identity);
    Map<String, Object> byUnion = union == null ? null : find("UNION_ID", union);
    Set<Long> candidates = new HashSet<>();
    if (byOpenid != null) {
      candidates.add(((Number) byOpenid.get("ID")).longValue());
    }
    if (byUnion != null) {
      candidates.add(((Number) byUnion.get("ID")).longValue());
    }
    for (String key : locks) {
      Long bound =
          jdbc.queryForObject(
              "SELECT user_id FROM CC_LOGIN_IDENTITY_LOCK WHERE identity_hash=? FOR UPDATE",
              Long.class,
              key);
      if (bound != null) {
        candidates.add(bound);
      }
    }
    if (candidates.size() > 1) {
      throw new IllegalArgumentException("历史账户关联冲突, 请联系商家核对");
    }
    Map<String, Object> found =
        candidates.isEmpty() ? null : find("ID", candidates.iterator().next());
    if (!candidates.isEmpty() && found == null) {
      throw new IllegalArgumentException("原绑定账户已不存在, 请联系商家核对");
    }
    return found;
  }

  /** 当前读放在身份锁之后, 并发请求能够看到前一事务刚提交的账户. */
  private Map<String, Object> find(String column, Object identity) {
    if (!List.of("ID", "MA_OPENID", "OPENID", "UNION_ID").contains(column)) {
      throw new IllegalArgumentException("身份类型无效");
    }
    List<Map<String, Object>> rows =
        jdbc.queryForList(
            "SELECT ID,MA_OPENID,OPENID,UNION_ID FROM WX_USER WHERE " + column + "=? FOR UPDATE",
            identity);
    if (rows.size() > 1) {
      throw new IllegalArgumentException("历史账户存在重复身份, 请联系商家核对");
    }
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** 空UnionID不参与关联, 不裁剪或改变不透明身份标识的大小写. */
  private static String identity(String value, int maximum, boolean optional) {
    if (optional && (value == null || value.isEmpty())) {
      return null;
    }
    if (value == null
        || value.isBlank()
        || value.length() > maximum
        || !value.equals(value.trim())) {
      throw new IllegalArgumentException("微信身份验证失败");
    }
    return value;
  }

  /** 身份锁只保存摘要, 不记录微信会话密钥、请求凭证或完整身份. */
  private static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("运行环境缺少SHA-256", error);
    }
  }
}
