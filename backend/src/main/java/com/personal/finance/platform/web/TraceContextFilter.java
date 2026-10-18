package com.personal.finance.platform.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TraceContextFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);
  public static final String TRACE_HEADER = "X-Trace-Id";
  public static final String MDC_KEY = "traceId";
  public static final String REQUEST_ATTRIBUTE = TraceContextFilter.class.getName() + ".traceId";

  private static final int MAX_TRACE_ID_LENGTH = 64;

  private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long startedAt = System.nanoTime();
    String traceId = resolveTraceId(request);
    request.setAttribute(REQUEST_ATTRIBUTE, traceId);
    MDC.put(MDC_KEY, traceId);
    response.setHeader(TRACE_HEADER, traceId);
    boolean unhandledException = false;

    try {
      filterChain.doFilter(request, response);
    } catch (IOException | ServletException | RuntimeException | Error e) {
      unhandledException = true;
      throw e;
    } finally {
      try {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String route = pattern != null ? pattern.toString() : "UNMAPPED";
        int status = response.getStatus();

        if (unhandledException && status < 400) {
          status = 500;
        }
        log.atInfo()
            .addKeyValue("event", "http_request_completed")
            .addKeyValue("httpMethod", request.getMethod())
            .addKeyValue("route", route)
            .addKeyValue("status", status)
            .addKeyValue("durationMs", durationMs)
            .addKeyValue("unhandledException", unhandledException)
            .log("http_request_completed");
      } finally {
        MDC.remove(MDC_KEY);
      }
    }

  }

  private static String resolveTraceId(HttpServletRequest request) {
    Enumeration<String> values = request.getHeaders(TRACE_HEADER);
    if (values == null || !values.hasMoreElements()) {
      return UUID.randomUUID().toString();
    }

    String candidate = values.nextElement();
    if (values.hasMoreElements()) {
      return UUID.randomUUID().toString();
    }

    if (candidate == null || candidate.length() > MAX_TRACE_ID_LENGTH
        || !TRACE_ID_PATTERN.matcher(candidate).matches()) {
      return UUID.randomUUID().toString();
    }

    return candidate;
  }
}
