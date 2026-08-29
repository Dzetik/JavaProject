package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class WebSocketSchedulerConfig {

    @Bean("jwtExpirationTaskScheduler")
    public TaskScheduler jwtExpirationTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("jwt-expiration-");

        scheduler.initialize();

        return scheduler;
    }
}