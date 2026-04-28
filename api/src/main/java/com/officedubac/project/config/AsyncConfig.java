package com.officedubac.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig
{
    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // nb de threads de base
        executor.setMaxPoolSize(10);      // nb maximum de threads
        executor.setQueueCapacity(100);   // taille de la queue
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
