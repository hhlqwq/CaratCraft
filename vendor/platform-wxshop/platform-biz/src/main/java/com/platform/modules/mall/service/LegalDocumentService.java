package com.platform.modules.mall.service;

import com.platform.common.exception.BusinessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 管理协议草稿、不可变发布正文及本人确认记录, 不代替经营者审定正文. */
@Service
public class LegalDocumentService {
    private final JdbcTemplate jdbc;

    /** 注入正文及确认记录的持久化接口. */
    public LegalDocumentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 两类文件必须一起明确确认, 版本来自用户实际查看的发布内容. */
    public record Acceptance(Integer termsVersion, Integer privacyVersion) {}

    /** 草稿携带读取时的修订号, 避免覆盖其他管理员的修改. */
    public record Draft(String kind, Integer revision, String title, String content) {}

    /** 返回全部当前发布正文, 尚未配置时返回空列表, 不回退草稿或伪造正文. */
    public List<Map<String, Object>> published() {
        return jdbc.queryForList(
                "SELECT d.kind,d.version,d.title,d.content,d.published_at FROM CC_LEGAL_DOCUMENT d"
                        + " JOIN CC_LEGAL_HEAD h ON h.kind=d.kind AND h.published_version=d.version"
                        + " WHERE d.status='PUBLISHED' ORDER BY d.kind");
    }

    /** 公开历史正文仅限曾发布版本, 供消费者核对确认记录. */
    public Map<String, Object> publishedVersion(String kind, int version) {
        validateKind(kind);
        var rows =
                jdbc.queryForList(
                        "SELECT kind,version,title,content,published_at FROM CC_LEGAL_DOCUMENT"
                                + " WHERE kind=? AND version=? AND status='PUBLISHED'",
                        kind,
                        version);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("该协议版本尚未发布或不存在");
        }
        return rows.get(0);
    }

    /** 管理端读取最新修订与历史版本, 操作者信息不进入公开接口. */
    public Map<String, Object> editor(String kind) {
        validateKind(kind);
        return Map.of(
                "head",
                head(kind, false),
                "versions",
                jdbc.queryForList(
                        "SELECT"
                            + " kind,version,title,content,status,operator_id,create_time,published_at"
                            + " FROM CC_LEGAL_DOCUMENT WHERE kind=? ORDER BY version DESC LIMIT 50",
                        kind));
    }

    /** 每次保存追加一个草稿修订, 已发布正文不接受原地编辑. */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> save(Draft draft, String operator) {
        if (draft == null) {
            throw new IllegalArgumentException("协议草稿不能为空");
        }
        validateKind(draft.kind());
        String title = draft.title() == null ? "" : draft.title().trim();
        String content = draft.content() == null ? "" : draft.content().trim();
        if (title.isEmpty()
                || title.length() > 120
                || content.isEmpty()
                || content.length() > 50000) {
            throw new IllegalArgumentException("请填写标题及正文, 标题最多120字, 正文最多50000字");
        }
        var head = head(draft.kind(), true);
        int revision = ((Number) head.get("revision")).intValue();
        if (draft.revision() == null || draft.revision() != revision) {
            throw new IllegalArgumentException("协议已被其他管理员修改, 请重新加载");
        }
        int next = Math.addExact(revision, 1);
        jdbc.update(
                "INSERT INTO CC_LEGAL_DOCUMENT(kind,version,title,content,operator_id)"
                        + " VALUES(?,?,?,?,?)",
                draft.kind(),
                next,
                title,
                content,
                operator);
        jdbc.update("UPDATE CC_LEGAL_HEAD SET revision=? WHERE kind=?", next, draft.kind());
        return editor(draft.kind());
    }

    /** 发布仅接受当前最新草稿, 防止迟到窗口误发布其他人正在编辑的版本. */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> publish(String kind, int version, String operator) {
        validateKind(kind);
        var head = head(kind, true);
        if (((Number) head.get("revision")).intValue() != version) {
            throw new IllegalArgumentException("草稿版本已变化, 请重新加载并核对正文");
        }
        int changed =
                jdbc.update(
                        "UPDATE CC_LEGAL_DOCUMENT SET status='PUBLISHED',published_at=NOW(),"
                                + "operator_id=? WHERE kind=? AND version=? AND status='DRAFT'",
                        operator,
                        kind,
                        version);
        if (changed != 1) {
            throw new IllegalArgumentException("只能发布最新未发布草稿");
        }
        jdbc.update("UPDATE CC_LEGAL_HEAD SET published_version=? WHERE kind=?", version, kind);
        return editor(kind);
    }

    /** 登录及独立确认入口共用服务端校验, 发布变化后必须重新展示并确认. */
    @Transactional(rollbackFor = Exception.class)
    public void accept(Long userId, Acceptance acceptance) {
        if (userId == null || userId <= 0 || acceptance == null) {
            throw new IllegalArgumentException("请阅读并主动同意当前用户协议及隐私政策");
        }
        // 固定锁顺序, 与并发发布串行核对, 避免混合新旧版本或只写入半套确认.
        int privacy = ((Number) head("PRIVACY", true).get("published_version")).intValue();
        int terms = ((Number) head("TERMS", true).get("published_version")).intValue();
        if (terms == 0
                || privacy == 0
                || !Integer.valueOf(terms).equals(acceptance.termsVersion())
                || !Integer.valueOf(privacy).equals(acceptance.privacyVersion())) {
            throw new IllegalArgumentException("协议尚未发布或版本已更新, 请重新阅读并确认");
        }
        for (var item : Map.of("TERMS", terms, "PRIVACY", privacy).entrySet()) {
            jdbc.update(
                    "INSERT INTO CC_LEGAL_ACCEPTANCE(user_id,kind,version) VALUES(?,?,?)"
                            + " ON DUPLICATE KEY UPDATE accepted_at=accepted_at",
                    userId,
                    item.getKey(),
                    item.getValue());
        }
    }

    /** 交换微信资料前先校验主动确认及发布版本, 记录时仍会加锁再次验证. */
    public void requireCurrent(Acceptance acceptance) {
        int privacy = ((Number) head("PRIVACY", false).get("published_version")).intValue();
        int terms = ((Number) head("TERMS", false).get("published_version")).intValue();
        if (acceptance == null
                || terms == 0
                || privacy == 0
                || !Integer.valueOf(terms).equals(acceptance.termsVersion())
                || !Integer.valueOf(privacy).equals(acceptance.privacyVersion())) {
            throw new IllegalArgumentException("请阅读并主动同意当前已发布的用户协议及隐私政策");
        }
    }

    /** 仅由登录身份查询本人历史确认, 不接受客户端提供其他用户标识. */
    public List<Map<String, Object>> history(Long userId) {
        return jdbc.queryForList(
                "SELECT kind,version,accepted_at FROM CC_LEGAL_ACCEPTANCE"
                        + " WHERE user_id=? ORDER BY accepted_at DESC,kind LIMIT 100",
                userId);
    }

    /** 仅新交易及新增个人数据处理要求当前版本, 不拦截既有订单查询和售后. */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> requireAccepted(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String kind : List.of("PRIVACY", "TERMS")) {
            int version = ((Number) head(kind, true).get("published_version")).intValue();
            if (version == 0) {
                throw new BusinessException("商家尚未发布完整协议, 暂不能进行新交易", 503);
            }
            var rows =
                    jdbc.queryForList(
                            "SELECT a.kind,a.version,a.accepted_at,d.title FROM CC_LEGAL_ACCEPTANCE"
                                    + " a JOIN CC_LEGAL_DOCUMENT d ON d.kind=a.kind AND"
                                    + " d.version=a.version WHERE a.user_id=? AND a.kind=? AND"
                                    + " a.version=? AND d.status='PUBLISHED' FOR SHARE",
                            userId,
                            kind,
                            version);
            if (rows.size() != 1) {
                throw new BusinessException("协议已有更新, 请阅读并主动确认当前版本后重试", 428);
            }
            result.add(rows.get(0));
        }
        return result;
    }

    /** 与订单插入处于同一事务, 保留首次确认时间及不可变发布版本. */
    @Transactional(rollbackFor = Exception.class)
    public void freezeForOrder(Integer orderId, Long userId) {
        Integer owned =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM mall_order WHERE id=? AND user_id=?",
                        Integer.class,
                        orderId,
                        userId);
        if (owned == null || owned != 1) {
            throw new IllegalArgumentException("订单不存在或不属于当前用户");
        }
        for (var document : requireAccepted(userId)) {
            jdbc.update(
                    "INSERT INTO CC_ORDER_LEGAL(order_id,user_id,kind,version,accepted_at)"
                            + " VALUES(?,?,?,?,?)",
                    orderId,
                    userId,
                    document.get("kind"),
                    document.get("version"),
                    document.get("accepted_at"));
        }
    }

    /** 已有订单只展示当时冻结版本, 不按商家当前指针回填历史事实. */
    public List<Map<String, Object>> orderDocuments(Integer orderId, Long userId) {
        return jdbc.queryForList(
                "SELECT a.kind,a.version,a.accepted_at,d.title FROM CC_ORDER_LEGAL a JOIN"
                        + " CC_LEGAL_DOCUMENT d ON d.kind=a.kind AND d.version=a.version WHERE"
                        + " a.order_id=? AND a.user_id=? ORDER BY a.kind",
                orderId,
                userId);
    }

    /** 锁定固定类别配置行, 让保存、发布和确认共享同一并发边界. */
    private Map<String, Object> head(String kind, boolean lock) {
        return jdbc.queryForMap(
                "SELECT kind,revision,published_version FROM CC_LEGAL_HEAD WHERE kind=?"
                        + (lock ? " FOR UPDATE" : ""),
                kind);
    }

    /** 类别只接受约定值, 不将任意用户输入用于表名或查询片段. */
    private static void validateKind(String kind) {
        if (!"TERMS".equals(kind) && !"PRIVACY".equals(kind)) {
            throw new IllegalArgumentException("协议类别无效");
        }
    }
}
