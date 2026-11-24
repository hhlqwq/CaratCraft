package com.platform.modules.community;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.platform.modules.custom.service.CustomDesignService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 社区独立业务, 审核前不可公开, 所有用户写入均从登录身份取归属. */
@Service
public class CommunityService {
    private static final Set<String> KINDS = Set.of("DESIGN", "FINISHED", "SUGGESTION");
    private static final Set<String> STATUSES = Set.of("PENDING", "PUBLISHED", "HIDDEN", "DELETED");
    private static final String PUBLIC_COLUMNS =
            "p.id,p.kind,p.title,p.content,CAST(p.images_json AS CHAR CHARACTER SET utf8mb4) AS"
                + " imagesJson,CAST(p.design_json AS CHAR CHARACTER SET utf8mb4) AS"
                + " designJson,p.create_time AS createTime,(SELECT COUNT(*) FROM CC_COMMUNITY_LIKE"
                + " l WHERE l.post_id=p.id) AS likes";
    private final JdbcTemplate jdbc;
    private final CustomDesignService designs;

    @org.springframework.beans.factory.annotation.Autowired
    private com.platform.modules.sys.service.BusinessOrganization businessOrganization;

    /** 注入社区数据与本人设计读取服务. */
    public CommunityService(JdbcTemplate jdbc, CustomDesignService designs) {
        this.jdbc = jdbc;
        this.designs = designs;
    }

    /** 登录用户锁串行化发布限额与幂等键, 防止并发重复发布. */
    private void lockUser(Long userId) {
        if (userId == null
                || jdbc.queryForList("SELECT id FROM WX_USER WHERE id=? FOR UPDATE", userId)
                        .isEmpty()) {
            throw new IllegalArgumentException("请先登录");
        }
    }

    /** 存储写入前先占用本人额度, 失败尝试同样计数, 不允许超限后继续落盘. */
    @Transactional(rollbackFor = Exception.class)
    public long reserveImage(Long userId) {
        lockUser(userId);
        Long count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_COMMUNITY_MEDIA WHERE user_id=? AND"
                                + " create_time>DATE_SUB(NOW(),INTERVAL 1 HOUR)",
                        Long.class,
                        userId);
        if (count != null && count >= 60) {
            throw new IllegalArgumentException("上传过于频繁, 请稍后再试");
        }
        return insert("INSERT INTO CC_COMMUNITY_MEDIA(user_id,url) VALUES(?,?)", userId, "");
    }

    /** 仅完成本人的安全上传占位, 同一占位不允许覆盖图片地址. */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerImage(Long userId, Long id, String url) {
        if (url == null
                || url.length() > 1000
                || !(url.startsWith("/upload/") || url.startsWith("https://"))) {
            throw new IllegalArgumentException("上传地址配置无效");
        }
        if (jdbc.update(
                        "UPDATE CC_COMMUNITY_MEDIA SET url=? WHERE id=? AND user_id=? AND url=''",
                        url,
                        id,
                        userId)
                != 1) {
            throw new IllegalArgumentException("图片上传状态已变化, 请重新上传");
        }
        return Map.of("id", id, "url", url);
    }

    /** 发布的设计只复制有序公开材料, 排除刻字、订单、价格配置和身体参数. */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publish(Long userId, CommunityPostRequest request) {
        if (request == null
                || request.getRequestKey() == null
                || !request.getRequestKey().matches("[A-Za-z0-9_-]{16,64}")) {
            throw new IllegalArgumentException("缺少有效发布请求标识");
        }
        lockUser(userId);
        String hash = requestHash(request);
        var existing =
                jdbc.queryForList(
                        "SELECT id,status,request_hash FROM CC_COMMUNITY_POST WHERE user_id=? AND"
                                + " request_key=?",
                        userId,
                        request.getRequestKey());
        if (!existing.isEmpty()) {
            var row = existing.get(0);
            if (!hash.equals(row.get("request_hash"))) {
                throw new IllegalArgumentException("发布标识已用于其他内容, 请重新发布");
            }
            return Map.of("id", row.get("id"), "status", row.get("status"));
        }
        if (!KINDS.contains(request.getKind() == null ? "" : request.getKind())) {
            throw new IllegalArgumentException("分享类型无效");
        }
        String title = text(request.getTitle(), 1, 60, "标题");
        String content = text(request.getContent(), "DESIGN".equals(request.getKind()) ? 0 : 1, 2000, "内容");
        Long count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_COMMUNITY_POST WHERE user_id=? AND"
                                + " create_time>DATE_SUB(NOW(),INTERVAL 1 DAY)",
                        Long.class,
                        userId);
        if (count != null && count >= 20) {
            throw new IllegalArgumentException("每天最多发布20条内容");
        }
        List<Long> mediaIds = request.getMediaIds() == null ? List.of() : request.getMediaIds();
        if (mediaIds.size() > 6
                || mediaIds.stream().anyMatch(id -> id == null || id <= 0)
                || Set.copyOf(mediaIds).size() != mediaIds.size()) {
            throw new IllegalArgumentException("最多选择6张本人上传图片, 不可重复");
        }
        List<String> images = new ArrayList<>();
        for (Long id : mediaIds) {
            var rows =
                    jdbc.queryForList(
                            "SELECT url FROM CC_COMMUNITY_MEDIA WHERE id=? AND user_id=?",
                            id,
                            userId);
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("图片不存在或不属于本人");
            }
            String imageUrl = String.valueOf(rows.get(0).get("url"));
            if (imageUrl.isBlank()) {
                throw new IllegalArgumentException("图片尚未上传完成");
            }
            images.add(imageUrl);
        }
        String designJson = null;
        if ("DESIGN".equals(request.getKind())) {
            if (!Boolean.TRUE.equals(request.getDesignConsent())
                    || request.getSnapshotId() == null) {
                throw new IllegalArgumentException("请选择本人设计并同意公开材料排列");
            }
            var snapshot = designs.getOwned(request.getSnapshotId(), userId);
            if ("ARCHIVED".equals(snapshot.getStatus())) {
                throw new IllegalArgumentException("已移除设计不能分享");
            }
            JSONObject quote = JSON.parseObject(snapshot.getPriceBreakdownJson());
            List<Map<String, Object>> publicItems = new ArrayList<>();
            var source = quote == null ? null : quote.getJSONArray("arrangement");
            if (source == null || source.isEmpty()) {
                throw new IllegalArgumentException("请先在排列编辑器中保存设计");
            }
            for (int i = 0; i < source.size(); i++) {
                JSONObject line = source.getJSONObject(i);
                Map<String, Object> copy = new LinkedHashMap<>();
                for (String key :
                        List.of(
                                "position",
                                "lengthRole",
                                "claspRole",
                                "axialMm",
                                "mountSpanMm",
                                "componentType",
                                "name",
                                "sizeMm",
                                "shape",
                                "luster",
                                "blemish")) {
                    copy.put(key, line.get(key));
                }
                publicItems.add(copy);
            }
            if (publicItems.stream().noneMatch(item -> "pearl".equals(item.get("componentType")))) {
                throw new IllegalArgumentException("分享设计至少需要一颗珍珠");
            }
            designJson = JSON.toJSONString(Map.of("arrangement", publicItems));
        } else if (request.getSnapshotId() != null) {
            throw new IllegalArgumentException("只有设计分享可以附带设计");
        }
        long id =
                insert(
                        "INSERT INTO"
                            + " CC_COMMUNITY_POST(user_id,request_key,request_hash,kind,title,content,images_json,design_json,scope_org_no)"
                            + " VALUES(?,?,?,?,?,?,?,?,?)",
                        userId,
                        request.getRequestKey(),
                        hash,
                        request.getKind(),
                        title,
                        content,
                        JSON.toJSONString(images),
                        designJson,
                        businessOrganization == null
                                ? null
                                : businessOrganization.defaultOrganization());
        return Map.of("id", id, "status", "PENDING");
    }

    /** 公共分页只读取已审核帖子, 不返回用户ID和私人设计ID. */
    public Map<String, Object> page(String kind, int page, int limit) {
        validatePage(page, limit);
        if (kind != null && !kind.isBlank() && !KINDS.contains(kind)) {
            throw new IllegalArgumentException("分享类型无效");
        }
        String filter = " FROM CC_COMMUNITY_POST p WHERE p.status='PUBLISHED'";
        List<Object> args = new ArrayList<>();
        if (kind != null && !kind.isBlank()) {
            filter += " AND p.kind=?";
            args.add(kind);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + filter, Long.class, args.toArray());
        args.add(limit);
        args.add((page - 1) * limit);
        return Map.of(
                "records",
                jdbc.queryForList(
                        "SELECT "
                                + PUBLIC_COLUMNS
                                + filter
                                + " ORDER BY p.id DESC LIMIT ? OFFSET ?",
                        args.toArray()),
                "total",
                total == null ? 0 : total);
    }

    /** 本人列表包含审核进度与拒绝原因, 排除本人已删除内容. */
    public Map<String, Object> mine(Long userId, int page, int limit) {
        validatePage(page, limit);
        Long total =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_COMMUNITY_POST WHERE user_id=? AND"
                                + " status<>'DELETED'",
                        Long.class,
                        userId);
        return Map.of(
                "records",
                jdbc.queryForList(
                        "SELECT "
                                + PUBLIC_COLUMNS
                                + ",p.status,p.version,p.moderation_reason AS moderationReason FROM"
                                + " CC_COMMUNITY_POST p WHERE p.user_id=? AND p.status<>'DELETED'"
                                + " ORDER BY p.id DESC LIMIT ? OFFSET ?",
                        userId,
                        limit,
                        (page - 1) * limit),
                "total",
                total == null ? 0 : total);
    }

    /** 公开详情与列表使用相同审核边界, 下架后立即不可读取. */
    public Map<String, Object> detail(Long id) {
        var rows =
                jdbc.queryForList(
                        "SELECT "
                                + PUBLIC_COLUMNS
                                + " FROM CC_COMMUNITY_POST p WHERE p.id=? AND p.status='PUBLISHED'",
                        id);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("内容不存在或尚未公开");
        }
        return rows.get(0);
    }

    /** 一次批量读取本人的点赞与归属, 不允许查询其他用户状态. */
    public List<Map<String, Object>> states(Long userId, List<Long> ids) {
        if (ids == null
                || ids.isEmpty()
                || ids.size() > 100
                || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("帖子列表无效");
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        List<Object> args = new ArrayList<>(List.of(userId, userId));
        args.addAll(ids);
        return jdbc.queryForList(
                "SELECT p.id,(p.user_id=?) AS owned,EXISTS(SELECT 1 FROM CC_COMMUNITY_LIKE l WHERE"
                        + " l.post_id=p.id AND l.user_id=?) AS liked FROM CC_COMMUNITY_POST p WHERE"
                        + " p.status='PUBLISHED' AND p.id IN ("
                        + placeholders
                        + ")",
                args.toArray());
    }

    /** 先锁帖子再写入唯一点赞关系, 并发审核或重复点击不导致计数漂移. */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> like(Long userId, Long id, boolean liked) {
        var post = locked(id);
        if (!"PUBLISHED".equals(post.get("status"))) {
            throw new IllegalArgumentException("该内容不可点赞");
        }
        if (liked) {
            jdbc.update(
                    "INSERT INTO CC_COMMUNITY_LIKE(post_id,user_id) VALUES(?,?) ON DUPLICATE KEY"
                            + " UPDATE post_id=VALUES(post_id)",
                    id,
                    userId);
        } else {
            jdbc.update("DELETE FROM CC_COMMUNITY_LIKE WHERE post_id=? AND user_id=?", id, userId);
        }
        return Map.of(
                "liked",
                liked,
                "likes",
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_COMMUNITY_LIKE WHERE post_id=?", Long.class, id));
    }

    /** 本人删除采用软删除, 不允许用户改变审核状态或恢复运营删除的内容. */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long id) {
        var post = locked(id);
        if (((Number) post.get("user_id")).longValue() != userId) {
            throw new IllegalArgumentException("只能删除本人分享");
        }
        if ("DELETED".equals(post.get("status"))) {
            jdbc.update("UPDATE CC_COMMUNITY_POST SET staff_deleted=0 WHERE id=?", id);
            return;
        }
        changeStatus(post, id, "DELETED", "用户本人", "本人删除");
    }

    /** 运营按授权机构和状态分页, 并区分可恢复的运营软删除. */
    public Map<String, Object> adminPage(
            String status,
            int page,
            int limit,
            com.platform.modules.sys.service.OrgActionScope scope) {
        validatePage(page, limit);
        if (status == null || !STATUSES.contains(status)) {
            throw new IllegalArgumentException("审核状态无效");
        }
        List<Object> parameters = new java.util.ArrayList<>();
        parameters.add(status);
        String filter =
                " FROM CC_COMMUNITY_POST p WHERE p.status=?"
                        + scope.andWhere("p.scope_org_no", parameters);
        Long total =
                jdbc.queryForObject("SELECT COUNT(*)" + filter, Long.class, parameters.toArray());
        parameters.add(limit);
        parameters.add((page - 1) * limit);
        return Map.of(
                "records",
                jdbc.queryForList(
                        "SELECT "
                                + PUBLIC_COLUMNS
                                + ",p.status,p.version,p.staff_deleted=1 AS restorable,"
                                + "p.moderation_reason AS moderationReason"
                                + filter
                                + " ORDER BY p.id DESC LIMIT ? OFFSET ?",
                        parameters.toArray()),
                "total",
                total == null ? 0L : total);
    }

    /** 审核必须匹配版本, 恢复仅适用于隐藏或运营软删除的内容. */
    @Transactional(rollbackFor = Exception.class)
    public void moderate(Long id, Long version, String status, String reason, String operator) {
        if (!Set.of("PUBLISHED", "HIDDEN", "DELETED", "RESTORE")
                .contains(status == null ? "" : status)) {
            throw new IllegalArgumentException("审核动作无效");
        }
        String explanation = text(reason, 1, 300, "操作原因");
        var post = locked(id);
        boolean deleted = "DELETED".equals(post.get("status"));
        boolean staffDeleted =
                post.get("staff_deleted") instanceof Number marker && marker.intValue() == 1;
        boolean restore = "RESTORE".equals(status);
        if (version == null
                || ((Number) post.get("version")).longValue() != version
                || (restore && !("HIDDEN".equals(post.get("status")) || (deleted && staffDeleted)))
                || (!restore && deleted)) {
            throw new IllegalArgumentException("内容已变化或不允许恢复, 请刷新");
        }
        String target = restore ? "PENDING" : status;
        changeStatus(post, id, target, operator, explanation);
        jdbc.update(
                "UPDATE CC_COMMUNITY_POST SET staff_deleted=? WHERE id=?",
                "DELETED".equals(target) ? 1 : 0,
                id);
    }

    /** 操作日志分页上限固定, 原始内容不会按HTML执行. */
    public List<Map<String, Object>> logs(Long id) {
        return jdbc.queryForList(
                "SELECT operator_name AS operatorName,from_status AS fromStatus,to_status AS"
                        + " toStatus,reason,create_time AS createTime FROM CC_COMMUNITY_LOG WHERE"
                        + " post_id=? ORDER BY id DESC LIMIT 200",
                id);
    }

    /** 帖子行锁统一所有点赞、审核与删除动作的顺序. */
    private Map<String, Object> locked(Long id) {
        var rows =
                jdbc.queryForList(
                        "SELECT id,user_id,status,version,staff_deleted FROM CC_COMMUNITY_POST"
                                + " WHERE id=? FOR UPDATE",
                        id);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("内容不存在");
        }
        return rows.get(0);
    }

    /** 状态变更和审计任一失败均回滚, 隐藏内容不能通过旧详情地址绕过审核. */
    private void changeStatus(
            Map<String, Object> post, Long id, String status, String operator, String reason) {
        if (jdbc.update(
                                "UPDATE CC_COMMUNITY_POST SET"
                                    + " status=?,moderation_reason=?,version=version+1,update_time=NOW()"
                                    + " WHERE id=?",
                                status,
                                reason,
                                id)
                        != 1
                || jdbc.update(
                                "INSERT INTO"
                                    + " CC_COMMUNITY_LOG(post_id,operator_name,from_status,to_status,reason)"
                                    + " VALUES(?,?,?,?,?)",
                                id,
                                operator,
                                post.get("status"),
                                status,
                                reason)
                        != 1) {
            throw new IllegalStateException("社区操作失败, 已回滚");
        }
    }

    /** JDBC返回数据库生成ID, 不使用全局最后插入ID. */
    private long insert(String sql, Object... args) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int changed =
                jdbc.update(
                        connection -> {
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            sql, Statement.RETURN_GENERATED_KEYS);
                            for (int i = 0; i < args.length; i++) {
                                statement.setObject(i + 1, args[i]);
                            }
                            return statement;
                        },
                        keys);
        if (changed != 1 || keys.getKey() == null) {
            throw new IllegalStateException("社区保存失败");
        }
        return keys.getKey().longValue();
    }

    /** 统一文本长度与纯文本边界, 避免空白刷帖. */
    private static String text(String value, int min, int max, String name) {
        if (value == null || value.trim().length() < min || value.trim().length() > max) {
            throw new IllegalArgumentException(name + "需要" + min + "到" + max + "字");
        }
        return value.trim();
    }

    /** 同一个幂等键必须对应同一提交意图, 防止修改正文后错误复用旧结果. */
    private static String requestHash(CommunityPostRequest request) {
        try {
            byte[] bytes =
                    JSON.toJSONString(request).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return java.util.HexFormat.of()
                    .formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException failure) {
            throw new IllegalStateException("当前环境不支持请求摘要", failure);
        }
    }

    /** 绑定分页上限并防止整数偏移溢出. */
    private static void validatePage(int page, int limit) {
        if (page < 1 || page > 100000 || limit < 1 || limit > 50) {
            throw new IllegalArgumentException("分页参数无效");
        }
    }
}
