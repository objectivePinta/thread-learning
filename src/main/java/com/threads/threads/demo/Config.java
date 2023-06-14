package com.threads.threads.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
public class Config {

    @Bean
    public ThreadPoolTaskExecutor myThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setMaxPoolSize(4);

        threadPoolTaskExecutor.setTaskDecorator(original -> {
            //this is still on main thread
            long t0 = System.currentTimeMillis();
            return () -> {
                long t1 = System.currentTimeMillis();
                // time waiting in queue
                log.info("XXXX {} seconds on thread {}. Already {} tasks in the queue with size {}",
                        (t1 - t0) / 1000, Thread.currentThread().getName(), threadPoolTaskExecutor.getQueueSize(), threadPoolTaskExecutor.getQueueCapacity());
                original.run();
            };
        });
        threadPoolTaskExecutor.setQueueCapacity(15);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        threadPoolTaskExecutor.setRejectedExecutionHandler((r, executor) -> {
            log.info("Will reject {}", r);
        });
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
