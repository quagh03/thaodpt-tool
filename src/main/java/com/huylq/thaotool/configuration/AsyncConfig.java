package com.huylq.thaotool.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
  @Bean(name = "asyncExecutor")
  public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.initialize();
    return executor;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    return RetryTemplate.builder().maxAttempts(3).fixedBackoff(2000).build();
  }
}
