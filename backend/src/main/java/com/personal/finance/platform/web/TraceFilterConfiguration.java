package com.personal.finance.platform.web;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class TraceFilterConfiguration {

  @Bean
  FilterRegistrationBean<TraceContextFilter> traceFilterRegistration() {

    FilterRegistrationBean<TraceContextFilter> registration = new FilterRegistrationBean<>(
        new TraceContextFilter());

    registration.setName("traceContextFilter");
    registration.addUrlPatterns("/*");

    registration.setDispatcherTypes(DispatcherType.REQUEST);

    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);

    return registration;
  }
}
