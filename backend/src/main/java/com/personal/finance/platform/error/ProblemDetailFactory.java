package com.personal.finance.platform.error;

import java.net.URI;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailFactory {
  public ProblemDetail create(
      HttpStatusCode status,
      URI type,
      String title,
      String detail,
      String code,
      URI instance,
      String traceId) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setType(type);
    problemDetail.setTitle(title);
    problemDetail.setInstance(instance);
    problemDetail.setProperty("code", code);
    problemDetail.setProperty("traceId", traceId);
    return problemDetail;
  }
}
