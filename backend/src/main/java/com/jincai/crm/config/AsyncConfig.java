package com.jincai.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    /**
     * 审计日志专用异步线程池
     * - 核心线程数 2，最大 4，队列 500，超出直接丢弃并打日志（调用方不感知）
     */
    @Bean(name = "auditLogExecutor")
    public TaskExecutor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-log-");
        executor.setRejectedExecutionHandler((r, pool) ->
            org.slf4j.LoggerFactory.getLogger(AsyncConfig.class)
                .warn("auditLogExecutor queue full, task discarded")
        );
        executor.initialize();
        return executor;
    }
}
