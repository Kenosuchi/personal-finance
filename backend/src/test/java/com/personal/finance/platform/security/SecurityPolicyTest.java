package com.personal.finance.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personal.finance.platform.error.ProblemDetailFactory;
import com.personal.finance.platform.error.SecurityProblemWriter;
import com.personal.finance.platform.filter.UnsafeOriginFilter;
import com.personal.finance.platform.web.TraceContextFilter;
import com.personal.finance.platform.web.TraceFilterConfiguration;

import jakarta.servlet.http.Cookie;

@WebMvcTest(
    controllers = {
        CsrfController.class,
        SecurityPolicyTest.TestOnlyUnsafeController.class
    },
    properties = {
        "debug=false",
        "finance.security.allowed-origin=http://localhost:4200"
    },
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({
    SecurityConfig.class,
    SecurityProblemWriter.class,
    ProblemDetailFactory.class,
    TraceFilterConfiguration.class,
    SecurityPolicyTest.TestOnlyUnsafeController.class,
    SecurityPolicyTest.TestUnsafeSecurityConfiguration.class
})
class SecurityPolicyTest {

  private static final String ALLOWED_ORIGIN = "http://localhost:4200";
  private static final String TEST_MUTATION_PATH = "/__test/security/csrf";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void bootstrapCreatesAngularCookieWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/csrf"))
        .andExpect(status().isNoContent())
        .andExpect(cookie().exists("XSRF-TOKEN"))
        .andExpect(cookie().httpOnly("XSRF-TOKEN", false))
        .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
        .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
  }

  @Test
  void missingTokenReturnsCorrelatedCsrfProblem() throws Exception {
    String traceId = "missing-csrf-trace";

    mockMvc.perform(post(TEST_MUTATION_PATH)
            .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
            .header(TraceContextFilter.TRACE_HEADER, traceId))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(header().string(TraceContextFilter.TRACE_HEADER, traceId))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.code").value("CSRF_REJECTED"))
        .andExpect(jsonPath("$.traceId").value(traceId))
        .andExpect(jsonPath("$.instance").value(TEST_MUTATION_PATH));
  }

  @Test
  void invalidTokenReturnsCsrfProblem() throws Exception {
    Cookie token = bootstrapToken();

    mockMvc.perform(post(TEST_MUTATION_PATH)
            .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
            .cookie(token)
            .header("X-XSRF-TOKEN", "invalid-token"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("CSRF_REJECTED"));
  }

  @Test
  void validAngularCookieAndHeaderReachTestOnlyHandler() throws Exception {
    Cookie token = bootstrapToken();

    mockMvc.perform(post(TEST_MUTATION_PATH)
            .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
            .cookie(token)
            .header("X-XSRF-TOKEN", token.getValue()))
        .andExpect(status().isOk());
  }

  @Test
  void externalOriginIsRejectedEvenWithValidToken() throws Exception {
    Cookie token = bootstrapToken();

    mockMvc.perform(post(TEST_MUTATION_PATH)
            .header(HttpHeaders.ORIGIN, "https://external.example")
            .cookie(token)
            .header("X-XSRF-TOKEN", token.getValue()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void unlistedPathAndDisallowedMethodReturnProblemWithoutAuthenticationChallenge() throws Exception {
    mockMvc.perform(get("/api/v1/transactions"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
        .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
        .andExpect(header().doesNotExist(HttpHeaders.LOCATION));

    Cookie token = bootstrapToken();
    mockMvc.perform(post("/api/csrf")
            .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
            .cookie(token)
            .header("X-XSRF-TOKEN", token.getValue()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
  }

  @Test
  void deniedRequestsKeepDefaultSecurityHeadersAndCorsClosed() throws Exception {
    mockMvc.perform(get("/unlisted"))
        .andExpect(status().isForbidden())
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("X-Frame-Options", "DENY"));

    mockMvc.perform(options(TEST_MUTATION_PATH)
            .header(HttpHeaders.ORIGIN, "https://external.example")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void noGeneratedUserDetailsServiceExists() {
    assertThat(applicationContext.getBeansOfType(UserDetailsService.class)).isEmpty();
  }

  private Cookie bootstrapToken() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/csrf"))
        .andExpect(status().isNoContent())
        .andReturn();
    Cookie token = result.getResponse().getCookie("XSRF-TOKEN");
    assertThat(token).isNotNull();
    return token;
  }

  @RestController
  static class TestOnlyUnsafeController {

    @PostMapping(TEST_MUTATION_PATH)
    void mutate() {
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestUnsafeSecurityConfiguration {

    @Bean
    @Order(0)
    SecurityFilterChain testUnsafeFilterChain(
        HttpSecurity http,
        SecurityProblemWriter problemWriter,
        @Value("${finance.security.allowed-origin}") String allowedOrigin) throws Exception {
      http
          .securityMatcher(TEST_MUTATION_PATH)
          .csrf(csrf -> csrf.spa())
          .authorizeHttpRequests(authorize -> authorize
              .requestMatchers(HttpMethod.POST, TEST_MUTATION_PATH).permitAll()
              .anyRequest().denyAll())
          .addFilterBefore(new UnsafeOriginFilter(allowedOrigin, problemWriter), CsrfFilter.class)
          .formLogin(AbstractHttpConfigurer::disable)
          .httpBasic(AbstractHttpConfigurer::disable)
          .logout(AbstractHttpConfigurer::disable)
          .requestCache(AbstractHttpConfigurer::disable)
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .exceptionHandling(exceptions -> exceptions
              .accessDeniedHandler((request, response, exception) -> {
                if (exception instanceof CsrfException) {
                  problemWriter.csrfRejected(request, response);
                } else {
                  problemWriter.accessDenied(request, response);
                }
              })
              .authenticationEntryPoint(
                  (request, response, exception) -> problemWriter.accessDenied(request, response)));
      return http.build();
    }
  }
}
