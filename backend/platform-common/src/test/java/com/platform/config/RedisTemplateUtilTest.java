package com.platform.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/** 验证删除缓存的单键、批量及空输入契约, 不连接业务Redis. */
class RedisTemplateUtilTest {
    /** 空参数没有删除目标, 不发送Redis命令. */
    @Test
    void emptyKeysDoNotDeleteAnything() {
        RedisTemplate<String, Object> template = mock();
        var utility = new RedisTemplateUtil(template);
        utility.del();
        utility.del((String[]) null);
        verifyNoInteractions(template);
    }

    /** 单个键仍使用单键删除接口, 不改变键名. */
    @Test
    void singleKeyUsesOriginalDeleteContract() {
        RedisTemplate<String, Object> template = mock();
        new RedisTemplateUtil(template).del("synthetic-one");
        verify(template).delete("synthetic-one");
        verifyNoMoreInteractions(template);
    }

    /** 多键保持原顺序并按有类型集合批量删除, 无未检查转换. */
    @Test
    void multipleKeysUseTypedCollection() {
        RedisTemplate<String, Object> template = mock();
        new RedisTemplateUtil(template).del("synthetic-one", "synthetic-two");
        verify(template).delete(List.of("synthetic-one", "synthetic-two"));
        verifyNoMoreInteractions(template);
    }
}
