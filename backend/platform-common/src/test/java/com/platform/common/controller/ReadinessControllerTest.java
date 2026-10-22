package com.platform.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;

/** 存活、就绪和错误响应脱敏回归, 不建立网络连接. */
class ReadinessControllerTest {
    /** 依赖缺失不影响存活, 但必须返回未就绪. */
    @Test
    @SuppressWarnings("unchecked")
    void missingDependenciesAreNotReady() {
        ObjectProvider<DataSource> databases = mock(ObjectProvider.class);
        ObjectProvider<RedisConnectionFactory> redis = mock(ObjectProvider.class);
        ReadinessController controller =
                new ReadinessController(databases, redis, "test", "revision");
        assertEquals(200, controller.live().getStatusCode().value());
        assertEquals(503, controller.ready().getStatusCode().value());
    }

    /** 数据库异常正文不得返回连接信息或密码. */
    @Test
    @SuppressWarnings("unchecked")
    void readinessDoesNotExposeConnectionSecrets() throws Exception {
        ObjectProvider<DataSource> databases = mock(ObjectProvider.class);
        ObjectProvider<RedisConnectionFactory> redis = mock(ObjectProvider.class);
        DataSource source = mock(DataSource.class);
        when(databases.getIfAvailable()).thenReturn(source);
        when(redis.getIfAvailable()).thenReturn(mock(RedisConnectionFactory.class));
        when(source.getConnection()).thenThrow(new java.sql.SQLException("password=DO_NOT_EXPOSE"));
        ReadinessController controller =
                new ReadinessController(databases, redis, "test", "revision");
        assertEquals(503, controller.ready().getStatusCode().value());
        assertFalse(controller.ready().getBody().toString().contains("DO_NOT_EXPOSE"));
    }
}
