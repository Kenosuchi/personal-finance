package com.personal.finance.platform.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.personal.finance.platform.error.SecurityProblemWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UnsafeOriginFilter extends OncePerRequestFilter {
  private final String allowedOrigin;
  private final SecurityProblemWriter problemWriter;

  public UnsafeOriginFilter(String allowedOrigin, SecurityProblemWriter problemWriter) {
    this.allowedOrigin = allowedOrigin;
    this.problemWriter = problemWriter;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    List<String> origins = Collections.list(request.getHeaders(HttpHeaders.ORIGIN));
    if (origins.size() != 1 || !allowedOrigin.equals(origins.getFirst())) {
      problemWriter.accessDenied(request, response);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
