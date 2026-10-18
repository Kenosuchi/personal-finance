package com.personal.finance.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "debug=false")
class FinancialJsonContractTest {

  @Autowired
  private JsonMapper jsonMapper;

  @Test
  void wholeVndAndBusinessDateRoundTripAsPlainStrings() throws Exception {
    FinanceContractFixture fixture = new FinanceContractFixture(
        "999999999999",
        LocalDate.of(2024, 2, 29));

    String json = jsonMapper.writeValueAsString(fixture);
    JsonNode document = jsonMapper.readTree(json);

    assertThat(document.get("amountVnd").isString()).isTrue();
    assertThat(document.get("amountVnd").stringValue()).isEqualTo("999999999999");
    assertThat(document.get("transactionDate").isString()).isTrue();
    assertThat(document.get("transactionDate").stringValue()).isEqualTo("2024-02-29");

    FinanceContractFixture decoded = jsonMapper.readValue(json, FinanceContractFixture.class);

    assertThat(decoded).isEqualTo(fixture);
    assertThat(new BigDecimal(decoded.amountVnd()).scale()).isZero();
    assertThat(new BigDecimal(decoded.amountVnd()).toPlainString())
        .isEqualTo(fixture.amountVnd());
    assertThat(json).doesNotContain("T00:00", "Z", "+00:00");
  }

  private record FinanceContractFixture(String amountVnd, LocalDate transactionDate) {
  }
}
