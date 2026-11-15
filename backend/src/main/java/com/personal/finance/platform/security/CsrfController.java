package com.personal.finance.platform.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
  @GetMapping("/api/csrf")
  ResponseEntity<Void> getCsrfToken(CsrfToken token) {
    token.getToken();
    return ResponseEntity.noContent().build();
  }
}
