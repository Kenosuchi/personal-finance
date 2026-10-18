package com.personal.finance.platform.error;

import java.net.URI;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.personal.finance.platform.web.TraceContextFilter;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String TRACE_ATTRIBUTE = TraceContextFilter.REQUEST_ATTRIBUTE;

  private static final URI PROBLEM_TYPE = URI.create("about:blank");

  // Reject field paths that could expose arbitrary map keys or user text.
  private static final Pattern SAFE_FIELD = Pattern.compile(
      "[A-Za-z][A-Za-z0-9_]*(?:\\[[0-9]{1,5}\\])?"
          + "(?:\\.[A-Za-z][A-Za-z0-9_]*(?:\\[[0-9]{1,5}\\])?)*");

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    // Ignore framework-provided bodies: they may contain unsafe details.
    ErrorInfo error = classify(ex, status);

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        status, error.detail());

    problem.setTitle(titleFor(status));
    problem.setProperty("code", error.code());

    if (status.value() == 400) {
      List<FieldIssue> issues = collectFieldErrors(ex);

      if (!issues.isEmpty()) {
        problem.setProperty("fieldErrors", issues);
      }
    }

    return super.handleExceptionInternal(
        ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> createResponseEntity(
      Object body,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    // All handled errors pass through this final enrichment point.
    ProblemDetail problem = (ProblemDetail) body;

    String traceId = resolveTraceId(request);

    problem.setType(PROBLEM_TYPE);
    problem.setProperty("traceId", traceId);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.addAll(headers);
    responseHeaders.set(TraceContextFilter.TRACE_HEADER, traceId);
    responseHeaders.setContentType(
        MediaType.parseMediaType("application/problem+json"));

    return super.createResponseEntity(
        problem, responseHeaders, status, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleUnexpected(
      Exception ex,
      WebRequest request) {

    log.atError()
        .addKeyValue("event", "unexpected_request_failure")
        .addKeyValue("errorCode", "INTERNAL_ERROR")
        .addKeyValue("exceptionType", ex.getClass().getName())
        .log("unexpected_request_failure");

    return handleExceptionInternal(
        ex,
        null,
        new HttpHeaders(),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request);
  }

  private static ErrorInfo classify(
      Exception ex,
      HttpStatusCode status) {

    // Check 5xx first: internal validation failures must not be
    // misrepresented as client validation failures.
    if (status.is5xxServerError()) {
      return new ErrorInfo(
          "INTERNAL_ERROR",
          "An unexpected server error occurred.");
    }

    if (ex instanceof MethodArgumentNotValidException
        || ex instanceof HandlerMethodValidationException) {
      return new ErrorInfo(
          "VALIDATION_FAILED",
          "Request validation failed.");
    }

    if (ex instanceof HttpMessageNotReadableException) {
      return new ErrorInfo(
          "MALFORMED_REQUEST",
          "Request body is invalid.");
    }

    if (ex instanceof TypeMismatchException) {
      return new ErrorInfo(
          "INVALID_PARAMETER",
          "A request parameter is invalid.");
    }

    return switch (status.value()) {
      case 400 -> new ErrorInfo(
          "INVALID_REQUEST",
          "The request is invalid.");
      case 404 -> new ErrorInfo(
          "NOT_FOUND",
          "The requested resource was not found.");
      case 405 -> new ErrorInfo(
          "METHOD_NOT_ALLOWED",
          "The HTTP method is not allowed.");
      case 415 -> new ErrorInfo(
          "UNSUPPORTED_MEDIA_TYPE",
          "The request content type is not supported.");
      default -> new ErrorInfo(
          "HTTP_" + status.value(),
          "The request could not be completed.");
    };
  }

  private static String titleFor(HttpStatusCode status) {
    HttpStatus known = HttpStatus.resolve(status.value());
    return known != null ? known.getReasonPhrase() : "HTTP Error";
  }

  private static String resolveTraceId(WebRequest request) {
    Object existing = request.getAttribute(
        TRACE_ATTRIBUTE, WebRequest.SCOPE_REQUEST);

    if (existing instanceof String id && !id.isBlank()) {
      return id;
    }

    // Fallback for MVC requests where the trace filter was not run.
    String generated = UUID.randomUUID().toString();
    request.setAttribute(
        TRACE_ATTRIBUTE, generated, WebRequest.SCOPE_REQUEST);
    return generated;
  }

  private static List<FieldIssue> collectFieldErrors(Exception ex) {
    TreeSet<String> fields = new TreeSet<>();

    if (ex instanceof MethodArgumentNotValidException validation) {
      for (FieldError error : validation.getBindingResult().getFieldErrors()) {
        fields.add(safeField(error.getField()));
      }
    } else if (ex instanceof HandlerMethodValidationException validation) {
      if (!validation.isForReturnValue()) {
        for (ParameterValidationResult result : validation.getParameterValidationResults()) {

          if (result instanceof ParameterErrors errors) {
            for (FieldError error : errors.getFieldErrors()) {
              fields.add(safeField(error.getField()));
            }
          } else {
            fields.add(safeField(parameterName(result)));
          }
        }
      }
    }

    return fields.stream()
        .map(field -> new FieldIssue(field, "Invalid value."))
        .toList();
  }

  private static String parameterName(ParameterValidationResult result) {
    MethodParameter parameter = result.getMethodParameter();

    RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);

    if (annotation != null && !annotation.name().isBlank()) {
      return annotation.name();
    }

    String name = parameter.getParameterName();
    return name != null ? name : "request";
  }

  private static String safeField(String field) {
    if (field == null || field.length() > 128
        || !SAFE_FIELD.matcher(field).matches()) {
      return "request";
    }
    return field;
  }

  private record ErrorInfo(String code, String detail) {
  }

  private record FieldIssue(String field, String message) {
  }
}
