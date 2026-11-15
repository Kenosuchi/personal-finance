package com.personal.finance.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.finance.platform.error.GlobalExceptionHandler;
import com.personal.finance.platform.error.ProblemDetailFactory;
import com.personal.finance.platform.web.TraceContextFilter;
import com.personal.finance.platform.web.TraceFilterConfiguration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest(
    properties = "debug=false",
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({
    ApiFoundationTest.TestOnlyController.class,
    ApiFoundationTest.TestSecurityConfiguration.class,
    GlobalExceptionHandler.class,
    ProblemDetailFactory.class,
    TraceFilterConfiguration.class
})
@ExtendWith(OutputCaptureExtension.class)
class ApiFoundationTest {

  private static final String UUID_PATTERN =
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void validationProblemUsesDocumentedShapeAndMatchingTraceId() throws Exception {
    String traceId = "validation-trace-123";

    mockMvc.perform(post("/__test/validation")
            .header(TraceContextFilter.TRACE_HEADER, traceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"description\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(header().string(TraceContextFilter.TRACE_HEADER, traceId))
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Request validation failed."))
        .andExpect(jsonPath("$.instance").value("/__test/validation"))
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.traceId").value(traceId))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("description"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("Invalid value."))
        .andExpect(jsonPath("$.fieldErrors[0].rejectedValue").doesNotExist());

    assertThat(MDC.get(TraceContextFilter.MDC_KEY)).isNull();
  }

  @Test
  void malformedJsonDoesNotExposeParserDetails() throws Exception {
    mockMvc.perform(post("/__test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"description\":"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
        .andExpect(jsonPath("$.detail").value("Request body is invalid."))
        .andExpect(jsonPath("$.traceId").isNotEmpty());
  }

  @Test
  void unexpectedFailureIsSanitizedAndCorrelatedInLogs(CapturedOutput output) throws Exception {
    String traceId = "failure-trace-456";
    String sensitiveDescription = "DO_NOT_LOG_DESCRIPTION_92741";

    mockMvc.perform(post("/__test/failure")
            .header(TraceContextFilter.TRACE_HEADER, traceId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"description\":\"" + sensitiveDescription + "\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(header().string(TraceContextFilter.TRACE_HEADER, traceId))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").value("An unexpected server error occurred."))
        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.traceId").value(traceId));

    assertThat(output).contains(traceId)
        .contains("unexpected_request_failure")
        .contains("http_request_completed")
        .doesNotContain(sensitiveDescription)
        .doesNotContain("SELECT description FROM transactions");
    assertThat(MDC.get(TraceContextFilter.MDC_KEY)).isNull();
  }

  @Test
  void invalidIncomingTraceIdIsReplacedAndNotLogged(CapturedOutput output) throws Exception {
    String invalidTraceId = "invalid trace id with spaces";

    MvcResult result = mockMvc.perform(get("/__test/success")
            .header(TraceContextFilter.TRACE_HEADER, invalidTraceId))
        .andExpect(status().isOk())
        .andReturn();

    String generatedTraceId = result.getResponse().getHeader(TraceContextFilter.TRACE_HEADER);
    assertThat(generatedTraceId).matches(UUID_PATTERN);
    assertThat(output).contains(generatedTraceId)
        .doesNotContain(invalidTraceId);
    assertThat(MDC.get(TraceContextFilter.MDC_KEY)).isNull();
  }

  @Test
  void multipleIncomingTraceIdsAreReplaced() throws Exception {
    MvcResult result = mockMvc.perform(get("/__test/success")
            .header(TraceContextFilter.TRACE_HEADER, "first-trace", "second-trace"))
        .andExpect(status().isOk())
        .andReturn();

    assertThat(result.getResponse().getHeader(TraceContextFilter.TRACE_HEADER))
        .matches(UUID_PATTERN)
        .isNotEqualTo("first-trace")
        .isNotEqualTo("second-trace");
  }

  @RestController
  static class TestOnlyController {

    @PostMapping("/__test/validation")
    void validate(@Valid @RequestBody TestRequest request) {
    }

    @PostMapping("/__test/failure")
    void fail(@RequestBody TestRequest request) {
      throw new IllegalStateException(
          "SELECT description FROM transactions: " + request.description());
    }

    @GetMapping("/__test/success")
    void succeed() {
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestSecurityConfiguration {

    @Bean
    SecurityFilterChain apiFoundationTestFilterChain(HttpSecurity http) throws Exception {
      http
          .csrf(csrf -> csrf.ignoringRequestMatchers("/__test/**"))
          .authorizeHttpRequests(authorize -> authorize
              .requestMatchers(
                  "/__test/validation",
                  "/__test/failure",
                  "/__test/success")
              .permitAll()
              .anyRequest().denyAll())
          .formLogin(AbstractHttpConfigurer::disable)
          .httpBasic(AbstractHttpConfigurer::disable)
          .logout(AbstractHttpConfigurer::disable)
          .requestCache(AbstractHttpConfigurer::disable);
      return http.build();
    }
  }

  private record TestRequest(@NotBlank String description) {
  }
}
