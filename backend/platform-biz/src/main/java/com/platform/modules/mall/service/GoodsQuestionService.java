package com.platform.modules.mall.service;

import com.platform.modules.sys.service.OrgActionScope;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 消费者商品提问与商家回答独立于共用常见问题和私密反馈. */
@Service
public class GoodsQuestionService {
    private final JdbcTemplate jdbc;

    /** 注入商品问答持久化接口. */
    public GoodsQuestionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 只返回在售商品经审核公开的问题及答复. */
    public Map<String, Object> published(Integer goodsId, int page) {
        requireGoodsId(goodsId);
        int offset = pageOffset(page);
        String from = """
                FROM CC_GOODS_QUESTION q JOIN mall_goods g ON q.goods_id=g.id
                WHERE q.goods_id=? AND q.status='PUBLISHED' AND g.is_on_sale=1 AND g.is_delete=0
                """;
        long total = jdbc.queryForObject("SELECT COUNT(*) " + from, Long.class, goodsId);
        var records = jdbc.queryForList(
                "SELECT q.id,q.question,q.answer " + from + " ORDER BY q.id DESC LIMIT 10 OFFSET ?",
                goodsId, offset);
        return Map.of("records", records, "total", total);
    }

    /** 以当前会话身份登记待审问题, 相同的待审重试合并返回. */
    @Transactional(rollbackFor = Exception.class)
    public void ask(Long userId, Integer goodsId, String question) {
        requireGoodsId(goodsId);
        String text = question == null ? "" : question.strip();
        if (userId == null || userId <= 0 || text.isBlank() || text.length() > 500) {
            throw new IllegalArgumentException("请输入 1 到 500 字的商品问题");
        }
        if (jdbc.queryForList("SELECT id FROM WX_USER WHERE id=? FOR UPDATE", Long.class, userId).isEmpty()) {
            throw new IllegalArgumentException("请先登录");
        }
        if (jdbc.queryForList(
                "SELECT id FROM mall_goods WHERE id=? AND is_on_sale=1 AND is_delete=0 FOR UPDATE",
                Integer.class, goodsId).isEmpty()) {
            throw new IllegalArgumentException("商品暂不可提问");
        }
        if (!jdbc.queryForList(
                "SELECT id FROM CC_GOODS_QUESTION WHERE user_id=? AND goods_id=? AND status='PENDING' AND question=? LIMIT 1",
                Long.class, userId, goodsId, text).isEmpty()) {
            return;
        }
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM CC_GOODS_QUESTION
                WHERE user_id=? AND create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)
                """, Long.class, userId);
        if (count != null && count >= 20) {
            throw new IllegalArgumentException("每天最多提交 20 个商品问题");
        }
        jdbc.update("INSERT INTO CC_GOODS_QUESTION(goods_id,user_id,question) VALUES (?,?,?)",
                goodsId, userId, text);
    }

    /** 列表范围以关联商品归属为准, 员工无跨范围访问. */
    public Map<String, Object> manage(OrgActionScope scope, int page) {
        int offset = pageOffset(page);
        List<Object> params = new ArrayList<>();
        String from = " FROM CC_GOODS_QUESTION q JOIN mall_goods g ON q.goods_id=g.id WHERE 1=1"
                + scope.andWhere("g.scope_org_no", params);
        long total = jdbc.queryForObject("SELECT COUNT(*)" + from, Long.class, params.toArray());
        params.add(offset);
        var records = jdbc.queryForList(
                "SELECT q.id,q.question,q.answer,q.status,q.create_time,g.name"
                        + from + " ORDER BY (q.status='PENDING') DESC,q.id DESC LIMIT 10 OFFSET ?",
                params.toArray());
        return Map.of("records", records, "total", total);
    }

    /** 商家审核回复后公开或隐藏, 写入语句同时按本动作的商品归属限制. */
    @Transactional(rollbackFor = Exception.class)
    public void moderate(OrgActionScope scope, Long id, String action, String answer) {
        boolean publish = "PUBLISH".equals(action);
        String text = answer == null ? "" : answer.strip();
        if (id == null || id < 1 || (!publish && !"HIDE".equals(action))) {
            throw new IllegalArgumentException("审核目标或操作无效");
        }
        if (publish && (text.isBlank() || text.length() > 1000)) {
            throw new IllegalArgumentException("请输入 1 到 1000 字的回答");
        }
        List<Object> params = new ArrayList<>();
        params.add(publish ? "PUBLISHED" : "HIDDEN");
        if (publish) params.add(text);
        params.add(id);
        int changed = jdbc.update("UPDATE CC_GOODS_QUESTION q JOIN mall_goods g ON q.goods_id=g.id"
                + " SET q.status=?" + (publish ? ",q.answer=?" : "")
                + " WHERE q.id=?" + scope.andWhere("g.scope_org_no", params), params.toArray());
        if (changed != 1) {
            throw new IllegalArgumentException("问题不存在或不在可处理范围内");
        }
    }

    /** 拒绝非法商品编号. */
    private static void requireGoodsId(Integer goodsId) {
        if (goodsId == null || goodsId < 1) {
            throw new IllegalArgumentException("商品编号无效");
        }
    }

    /** 保持有限分页并防止偏移整数溢出. */
    private static int pageOffset(int page) {
        if (page < 1 || page > 10000) throw new IllegalArgumentException("分页参数不正确");
        return (page - 1) * 10;
    }
}
