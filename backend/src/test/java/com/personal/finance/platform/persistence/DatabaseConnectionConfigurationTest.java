package com.personal.finance.platform.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DatabaseConnectionConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(DatabaseConnectionConfiguration.class);

  @Test
  void missingDatabaseSettingsAreReportedByName() {
    contextRunner.run(context -> {
      assertThat(context).hasFailed();

      String failure = stackTrace(context.getStartupFailure());
      assertThat(failure)
          .contains("FINANCE_DATABASE_RUNTIME_URL")
          .contains("FINANCE_DATABASE_RUNTIME_USERNAME")
          .contains("FINANCE_DATABASE_RUNTIME_PASSWORD")
          .contains("FINANCE_DATABASE_MIGRATION_URL")
          .contains("FINANCE_DATABASE_MIGRATION_USERNAME")
          .contains("FINANCE_DATABASE_MIGRATION_PASSWORD");
    });
  }

  @Test
  void configurationFailureDoesNotExposeSuppliedSecrets() {
    String runtimeSecret = "DO_NOT_EXPOSE_RUNTIME_SECRET_8421";
    String migrationSecret = "DO_NOT_EXPOSE_MIGRATION_SECRET_8421";

    contextRunner
        .withPropertyValues(
            "finance.database.runtime.password=" + runtimeSecret,
            "finance.database.migration.password=" + migrationSecret)
        .run(context -> {
          assertThat(context).hasFailed();

          assertThat(stackTrace(context.getStartupFailure()))
              .contains("FINANCE_DATABASE_RUNTIME_URL")
              .contains("FINANCE_DATABASE_MIGRATION_URL")
              .doesNotContain(runtimeSecret)
              .doesNotContain(migrationSecret);
        });
  }

  private static String stackTrace(Throwable failure) {
    StringWriter output = new StringWriter();
    failure.printStackTrace(new PrintWriter(output));
    return output.toString();
  }
}
