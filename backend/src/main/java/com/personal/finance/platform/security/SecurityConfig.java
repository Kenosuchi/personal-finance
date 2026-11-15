package com.personal.finance.platform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfFilter;

import com.personal.finance.platform.error.SecurityProblemWriter;
import com.personal.finance.platform.filter.UnsafeOriginFilter;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
  @Value("${finance.security.allowed-origin}")
  private String allowedOrigin;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, SecurityProblemWriter problemWriter) throws Exception {
    http
        .csrf(csrf -> csrf.spa())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.GET,
                "/api/csrf",
                "/actuator/health/liveness",
                "/actuator/health/readiness")
            .permitAll().anyRequest().denyAll())
        .addFilterBefore(new UnsafeOriginFilter(allowedOrigin, problemWriter), CsrfFilter.class)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .requestCache(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exceptions -> exceptions.accessDeniedHandler((request, response, exception) -> {
          if (exception instanceof CsrfException) {
            problemWriter.csrfRejected(request, response);
          } else {
            problemWriter.accessDenied(request, response);
          }
        }).authenticationEntryPoint((request, response, exception) -> problemWriter.accessDenied(request, response)));
    return http.build();
  }

}
