package com.platform.support;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/** 为真实数据库回归创建独立事务通知, 不使用废弃构造接口. */
public final class TransactionTestSupport {
    /** 事务对象逐个创建, 不共享跨测试状态. */
    private TransactionTestSupport() {}

    /** 显式设置真实事务管理器和原有注解属性源, 保留代理回滚语义. */
    public static TransactionInterceptor interceptor(PlatformTransactionManager manager) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        return interceptor;
    }
}
