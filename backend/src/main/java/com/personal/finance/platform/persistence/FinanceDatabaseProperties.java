package com.personal.finance.platform.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("finance.database")
public class FinanceDatabaseProperties {

  @Valid
  private final RuntimeConnection runtime = new RuntimeConnection();

  @Valid
  private final MigrationConnection migration = new MigrationConnection();

  public RuntimeConnection getRuntime() {
    return runtime;
  }

  public MigrationConnection getMigration() {
    return migration;
  }

  public static final class RuntimeConnection {

    @NotBlank(message = "required setting FINANCE_DATABASE_RUNTIME_URL is missing")
    private String url;

    @NotBlank(message = "required setting FINANCE_DATABASE_RUNTIME_USERNAME is missing")
    private String username;

    @NotBlank(message = "required setting FINANCE_DATABASE_RUNTIME_PASSWORD is missing")
    private String password;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public static final class MigrationConnection {

    @NotBlank(message = "required setting FINANCE_DATABASE_MIGRATION_URL is missing")
    private String url;

    @NotBlank(message = "required setting FINANCE_DATABASE_MIGRATION_USERNAME is missing")
    private String username;

    @NotBlank(message = "required setting FINANCE_DATABASE_MIGRATION_PASSWORD is missing")
    private String password;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
