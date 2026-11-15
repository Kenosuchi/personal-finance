package com.personal.finance.platform.error;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;

import com.personal.finance.platform.web.TraceContextFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SecurityProblemWriter {
  private final ProblemDetailFactory factory;
  private final JacksonJsonHttpMessageConverter jsonConverter;

  public SecurityProblemWriter(
      ProblemDetailFactory problemDetailFactory,
      JsonMapper jsonMapper) {
    this.factory = problemDetailFactory;
    this.jsonConverter = new JacksonJsonHttpMessageConverter(jsonMapper.rebuild());
  }

  public void csrfRejected(HttpServletRequest request, HttpServletResponse response) throws IOException {
    writeForbidden(request, response, URI.create("personal-finance:problem:csrf-rejected"), "CSRF_REJECTED",
        "The request could not be verified.");
  }

  public void accessDenied(HttpServletRequest request, HttpServletResponse response) throws IOException {
    writeForbidden(request, response, URI.create("personal-finance:problem:access-denied"),
        "ACCESS_DENIED",
        "Access to this resource is denied.");
  }

  private void writeForbidden(HttpServletRequest request, HttpServletResponse response, URI type, String code,
      String detail) throws IOException {
    String traceId = (String) request.getAttribute(TraceContextFilter.REQUEST_ATTRIBUTE);
    ProblemDetail problemDetail = factory.create(HttpStatus.FORBIDDEN, type, "Forbidden", detail, code,
        URI.create(request.getRequestURI()), traceId);
    response.setStatus(problemDetail.getStatus());
    response.setHeader(TraceContextFilter.TRACE_HEADER, traceId);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    if (HttpMethod.HEAD.matches(request.getMethod())) {
      return;
    }
    jsonConverter.write(problemDetail, MediaType.APPLICATION_PROBLEM_JSON, new ServletServerHttpResponse(response));
  }
}
