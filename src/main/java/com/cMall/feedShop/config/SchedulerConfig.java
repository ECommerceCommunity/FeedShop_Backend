package com.cMall.feedShop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 스케줄러가 사용할 스레드 풀 설정
        // 기본값은 단일 스레드이므로, 여러 스케줄러 작업이 동시에 실행될 수 있도록 스레드 풀 설정
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }
}
