package com.personal.finance.platform.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class DatabaseEnvironmentConfigurationTest {

  private static final String[] REQUIRED_SETTINGS = {
      "FINANCE_DATABASE_RUNTIME_URL",
      "FINANCE_DATABASE_RUNTIME_USERNAME",
      "FINANCE_DATABASE_RUNTIME_PASSWORD",
      "FINANCE_DATABASE_MIGRATION_URL",
      "FINANCE_DATABASE_MIGRATION_USERNAME",
      "FINANCE_DATABASE_MIGRATION_PASSWORD"
  };

  private static final String[] REQUIRED_PROPERTIES = {
      "spring.datasource.url",
      "spring.datasource.username",
      "spring.datasource.password",
      "spring.flyway.url",
      "spring.flyway.user",
      "spring.flyway.password"
  };

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withInitializer(new ConfigDataApplicationContextInitializer())
      .withUserConfiguration(DatabaseSettingsProbeConfiguration.class);

  @ParameterizedTest
  @ValueSource(strings = {
      "FINANCE_DATABASE_RUNTIME_URL",
      "FINANCE_DATABASE_RUNTIME_USERNAME",
      "FINANCE_DATABASE_RUNTIME_PASSWORD",
      "FINANCE_DATABASE_MIGRATION_URL",
      "FINANCE_DATABASE_MIGRATION_USERNAME",
      "FINANCE_DATABASE_MIGRATION_PASSWORD"
  })
  void missingDatabaseSettingIsReportedByNameWithoutExposingConfiguredValues(
      String missingSetting) {
    String[] configuredSettings = Arrays.stream(REQUIRED_SETTINGS)
        .map(setting -> setting.equals(missingSetting)
            ? setting + "=${INTENTIONALLY_MISSING_" + setting + "}"
            : setting + "=DO_NOT_EXPOSE_" + setting)
        .toArray(String[]::new);

    contextRunner.withPropertyValues(configuredSettings).run(context -> {
      assertThat(context).hasFailed();

      String failure = stackTrace(context.getStartupFailure());
      assertThat(failure).contains(missingSetting);
      Arrays.stream(configuredSettings)
          .map(setting -> setting.substring(setting.indexOf('=') + 1))
          .filter(value -> value.startsWith("DO_NOT_EXPOSE_"))
          .forEach(value -> assertThat(failure).doesNotContain(value));
    });
  }

  private static String stackTrace(Throwable failure) {
    StringWriter output = new StringWriter();
    failure.printStackTrace(new PrintWriter(output));
    return output.toString();
  }

  @Configuration(proxyBeanMethods = false)
  static class DatabaseSettingsProbeConfiguration {

    @Bean
    DatabaseSettingsProbe databaseSettingsProbe(Environment environment) {
      Arrays.stream(REQUIRED_PROPERTIES)
          .map(environment::getRequiredProperty)
          .forEach(environment::resolveRequiredPlaceholders);
      return new DatabaseSettingsProbe();
    }
  }

  private static final class DatabaseSettingsProbe {
  }
}
