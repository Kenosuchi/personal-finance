package com.personal.finance.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.mysql.MySQLContainer;

import com.personal.finance.FinanceApplication;
import com.personal.finance.categories.domain.CategoryType;
import com.personal.finance.categories.persistence.CategoryEntity;
import com.personal.finance.transactions.domain.TransactionType;
import com.personal.finance.transactions.persistence.TransactionEntity;
import com.personal.schemafixture.IncompatibleMappingConfiguration;

import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MySqlSchemaIntegrationTest {

  private static final String MYSQL_IMAGE = "mysql:8.4.11";
  private static final String DATABASE_NAME = "finance_test";
  private static final String RUNTIME_USERNAME = "finance_runtime";
  private static final String RUNTIME_PASSWORD = "runtime_test_password";
  private static final String MIGRATION_USERNAME = "finance_migration";
  private static final String MIGRATION_PASSWORD = "migration_test_password";

  private static final MySQLContainer MYSQL = new MySQLContainer(MYSQL_IMAGE)
      .withDatabaseName(DATABASE_NAME)
      .withUsername("container_bootstrap")
      .withPassword("container_bootstrap_password");

  static {
    MYSQL.start();
    provisionDatabaseUsers();
  }

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MySqlSchemaIntegrationTest::jdbcUrl);
    registry.add("spring.datasource.username", () -> RUNTIME_USERNAME);
    registry.add("spring.datasource.password", () -> RUNTIME_PASSWORD);
    registry.add("spring.flyway.url", MySqlSchemaIntegrationTest::jdbcUrl);
    registry.add("spring.flyway.user", () -> MIGRATION_USERNAME);
    registry.add("spring.flyway.password", () -> MIGRATION_PASSWORD);
  }

  @Autowired
  private Flyway flyway;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Test
  void emptySchemaIsMigratedAndHibernateMappingsValidate() {
    assertThat(flyway.info().applied()).hasSize(2);
    assertThat(jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name IN ('categories', 'transactions')
          AND engine = 'InnoDB'
          AND table_collation = 'utf8mb4_0900_ai_ci'
        """,
        Integer.class)).isEqualTo(2);
    assertThat(jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND (
            (table_name = 'categories'
              AND column_name = 'normalized_name'
              AND collation_name = 'utf8mb4_0900_bin')
            OR (table_name = 'transactions'
              AND column_name = 'amount_vnd'
              AND data_type = 'decimal'
              AND numeric_precision = 19
              AND numeric_scale = 0)
            OR (table_name = 'transactions'
              AND column_name = 'transaction_date'
              AND data_type = 'date')
          )
        """,
        Integer.class)).isEqualTo(3);
    assertThat(jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*)
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
          AND constraint_name IN (
            'chk_categories_type',
            'chk_categories_name_not_blank',
            'chk_categories_normalized_name_not_blank',
            'uq_categories_type_normalized_name',
            'chk_transactions_type',
            'chk_transactions_amount',
            'chk_transactions_expense_description',
            'fk_transactions_category'
          )
        """,
        Integer.class)).isEqualTo(8);
    assertThat(jdbcTemplate.queryForObject(
        """
        SELECT COUNT(DISTINCT index_name)
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND index_name IN (
            'uq_categories_type_normalized_name',
            'idx_transactions_date_id',
            'idx_transactions_category_date_id'
          )
        """,
        Integer.class)).isEqualTo(3);
    assertThat(jdbcTemplate.queryForObject("SELECT @@session.time_zone", String.class))
        .isEqualTo("+00:00");
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class))
        .isZero();
  }

  @Test
  void repeatedApplicationStartupDoesNotReapplyMigrations() {
    int historyRowsBefore = migrationHistoryRows();

    try (ConfigurableApplicationContext ignored = new SpringApplicationBuilder(
        FinanceApplication.class)
        .properties(applicationProperties())
        .run()) {
      assertThat(ignored.isActive()).isTrue();
    }

    assertThat(migrationHistoryRows()).isEqualTo(historyRowsBefore);
    assertThat(flyway.migrate().migrationsExecuted).isZero();
  }

  @Test
  void exactAmountAndBusinessDateRoundTripThroughJpa() {
    TransactionTemplate transaction = new TransactionTemplate(transactionManager);

    transaction.executeWithoutResult(status -> {
      CategoryEntity category = new CategoryEntity(
          CategoryType.INCOME, "Salary", "salary-jpa-roundtrip");
      entityManager.persist(category);
      entityManager.flush();

      TransactionEntity entry = new TransactionEntity(
          TransactionType.INCOME,
          new BigDecimal("999999999999"),
          LocalDate.of(2024, 2, 29),
          category.getId(),
          null);
      entityManager.persist(entry);
      entityManager.flush();

      Long entryId = entry.getId();
      entityManager.clear();

      TransactionEntity persisted = entityManager.find(TransactionEntity.class, entryId);
      assertThat(persisted.getAmountVnd()).isEqualByComparingTo("999999999999");
      assertThat(persisted.getTransactionDate()).isEqualTo(LocalDate.of(2024, 2, 29));
      assertThat(persisted.getVersion()).isZero();
      assertThat(persisted.getCreatedAt()).isNotNull();
      assertThat(persisted.getUpdatedAt()).isNotNull();
      status.setRollbackOnly();
    });
  }

  @Test
  @Transactional
  void schemaRejectsInvalidRowsAndAllowsNameReuseAcrossTypes() {
    long categoryId = insertCategory("INCOME", uniqueName("valid-income"));

    assertThatThrownBy(() -> insertTransaction("INCOME", BigDecimal.ZERO, categoryId, null))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> insertTransaction(
        "INCOME", new BigDecimal("-1"), categoryId, null))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> insertTransaction(
        "INCOME", new BigDecimal("1000000000000"), categoryId, null))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> insertTransaction("UNSUPPORTED", BigDecimal.ONE, categoryId, null))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> insertTransaction("EXPENSE", BigDecimal.ONE, categoryId, null))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> insertTransaction("EXPENSE", BigDecimal.ONE, categoryId, "   "))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> insertTransaction("INCOME", BigDecimal.ONE, Long.MAX_VALUE, null))
        .isInstanceOf(RuntimeException.class);

    String duplicateName = uniqueName("duplicate");
    insertCategory("INCOME", duplicateName);
    assertThatThrownBy(() -> insertCategory("INCOME", duplicateName))
        .isInstanceOf(RuntimeException.class);
    assertThat(insertCategory("EXPENSE", duplicateName)).isPositive();

    assertThatThrownBy(() -> insertCategory("INVALID", uniqueName("invalid-type")))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @Transactional
  void applicationOnlyRulesAreNotClaimedAsSchemaChecks() {
    long expenseCategoryId = insertCategory("EXPENSE", uniqueName("expense-category"));

    assertThat(jdbcTemplate.update(
        """
        INSERT INTO transactions
          (type, amount_vnd, transaction_date, category_id, description)
        VALUES (?, ?, ?, ?, ?)
        """,
        "INCOME",
        BigDecimal.ONE,
        LocalDate.now().plusDays(1),
        expenseCategoryId,
        null)).isOne();

    assertThat(jdbcTemplate.update(
        """
        INSERT INTO transactions
          (type, amount_vnd, transaction_date, category_id, description)
        VALUES (?, ?, ?, ?, ?)
        """,
        "INCOME",
        new BigDecimal("1.5"),
        LocalDate.of(2024, 2, 29),
        expenseCategoryId,
        null)).isOne();
    assertThat(jdbcTemplate.queryForObject(
        "SELECT amount_vnd FROM transactions WHERE id = LAST_INSERT_ID()",
        BigDecimal.class)).isEqualByComparingTo("2");

    assertThat(jdbcTemplate.update(
        "INSERT INTO categories (type, name, normalized_name) VALUES (?, ?, ?)",
        "INCOME",
        "  Mixed Case  ",
        uniqueName("Not-Normalized"))).isOne();
  }

  @Test
  void runtimeUserCanCrudButCannotChangeSchema() throws SQLException {
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Integer.class))
        .isNotNull();

    long categoryId = insertCategory("INCOME", uniqueName("runtime-crud"));
    assertThat(jdbcTemplate.update(
        "UPDATE categories SET name = ? WHERE id = ?", "Updated", categoryId)).isOne();
    assertThat(jdbcTemplate.update("DELETE FROM categories WHERE id = ?", categoryId)).isOne();

    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      assertThatThrownBy(() -> statement.execute(
          "CREATE TABLE runtime_ddl_probe (id BIGINT NOT NULL)"))
          .isInstanceOf(SQLException.class);
    }

    String probe = "migration_ddl_probe";
    try (Connection connection = DriverManager.getConnection(
        jdbcUrl(), MIGRATION_USERNAME, MIGRATION_PASSWORD);
        Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE " + probe + " (id BIGINT NOT NULL)");
      statement.execute("DROP TABLE " + probe);
    }
  }

  @Test
  void alteredMigrationChecksumFailsValidation(@TempDir Path migrationDirectory)
      throws IOException {
    String v1 = classpathText("db/migration/V1__create_categories.sql");
    String v2 = classpathText("db/migration/V2__create_transactions.sql");
    Files.writeString(
        migrationDirectory.resolve("V1__create_categories.sql"),
        "-- checksum mismatch probe\n" + v1,
        StandardCharsets.UTF_8);
    Files.writeString(
        migrationDirectory.resolve("V2__create_transactions.sql"),
        v2,
        StandardCharsets.UTF_8);

    Flyway altered = Flyway.configure()
        .dataSource(jdbcUrl(), MIGRATION_USERNAME, MIGRATION_PASSWORD)
        .locations("filesystem:" + migrationDirectory.toAbsolutePath())
        .cleanDisabled(true)
        .load();

    assertThatThrownBy(altered::validate)
        .hasMessageContaining("checksum mismatch");
  }

  @Test
  void incompatibleMappedSchemaFailsStartup() {
    assertThatThrownBy(() -> new SpringApplicationBuilder(
        FinanceApplication.class,
        IncompatibleMappingConfiguration.class)
        .properties(applicationProperties())
        .run())
        .hasStackTraceContaining("Schema validation")
        .hasStackTraceContaining("missing_mapping_probe");
  }

  private int migrationHistoryRows() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1",
        Integer.class);
  }

  private long insertCategory(String type, String normalizedName) {
    jdbcTemplate.update(
        "INSERT INTO categories (type, name, normalized_name) VALUES (?, ?, ?)",
        type,
        normalizedName,
        normalizedName);
    return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
  }

  private void insertTransaction(
      String type,
      BigDecimal amount,
      long categoryId,
      String description) {
    jdbcTemplate.update(
        """
        INSERT INTO transactions
          (type, amount_vnd, transaction_date, category_id, description)
        VALUES (?, ?, ?, ?, ?)
        """,
        type,
        amount,
        LocalDate.of(2024, 2, 29),
        categoryId,
        description);
  }

  private static String uniqueName(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private static String classpathText(String location) throws IOException {
    return new ClassPathResource(location).getContentAsString(StandardCharsets.UTF_8);
  }

  private static String jdbcUrl() {
    String separator = MYSQL.getJdbcUrl().contains("?") ? "&" : "?";
    return MYSQL.getJdbcUrl() + separator
        + "connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true";
  }

  private static Map<String, Object> applicationProperties() {
    return Map.of(
        "spring.main.web-application-type", "none",
        "spring.main.banner-mode", "off",
        "FINANCE_DATABASE_RUNTIME_URL", jdbcUrl(),
        "FINANCE_DATABASE_RUNTIME_USERNAME", RUNTIME_USERNAME,
        "FINANCE_DATABASE_RUNTIME_PASSWORD", RUNTIME_PASSWORD,
        "FINANCE_DATABASE_MIGRATION_URL", jdbcUrl(),
        "FINANCE_DATABASE_MIGRATION_USERNAME", MIGRATION_USERNAME,
        "FINANCE_DATABASE_MIGRATION_PASSWORD", MIGRATION_PASSWORD);
  }

  private static void provisionDatabaseUsers() {
    try (Connection connection = DriverManager.getConnection(
        MYSQL.getJdbcUrl(), "root", MYSQL.getPassword());
        Statement statement = connection.createStatement()) {
      statement.execute("CREATE USER '" + RUNTIME_USERNAME
          + "'@'%' IDENTIFIED BY '" + RUNTIME_PASSWORD + "'");
      statement.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON `" + DATABASE_NAME
          + "`.* TO '" + RUNTIME_USERNAME + "'@'%'");
      statement.execute("CREATE USER '" + MIGRATION_USERNAME
          + "'@'%' IDENTIFIED BY '" + MIGRATION_PASSWORD + "'");
      statement.execute("GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, "
          + "REFERENCES ON `" + DATABASE_NAME + "`.* TO '"
          + MIGRATION_USERNAME + "'@'%'");
    } catch (SQLException exception) {
      MYSQL.stop();
      throw new ExceptionInInitializerError(exception);
    }
  }

}
