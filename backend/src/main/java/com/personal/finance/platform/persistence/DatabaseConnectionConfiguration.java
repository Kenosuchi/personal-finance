package com.personal.finance.platform.persistence;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayConnectionDetails;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FinanceDatabaseProperties.class)
class DatabaseConnectionConfiguration {

  @Bean
  JdbcConnectionDetails runtimeConnectionDetails(FinanceDatabaseProperties properties) {
    FinanceDatabaseProperties.RuntimeConnection runtime = properties.getRuntime();

    return new JdbcConnectionDetails() {
      @Override
      public String getUsername() {
        return runtime.getUsername();
      }

      @Override
      public String getPassword() {
        return runtime.getPassword();
      }

      @Override
      public String getJdbcUrl() {
        return runtime.getUrl();
      }
    };
  }

  @Bean
  FlywayConnectionDetails migrationConnectionDetails(FinanceDatabaseProperties properties) {
    FinanceDatabaseProperties.MigrationConnection migration = properties.getMigration();

    return new FlywayConnectionDetails() {
      @Override
      public String getUsername() {
        return migration.getUsername();
      }

      @Override
      public String getPassword() {
        return migration.getPassword();
      }

      @Override
      public String getJdbcUrl() {
        return migration.getUrl();
      }
    };
  }
}
