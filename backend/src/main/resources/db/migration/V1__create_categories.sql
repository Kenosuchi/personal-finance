CREATE TABLE categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(10) NOT NULL,
  name VARCHAR(80) NOT NULL,
  normalized_name VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin NOT NULL,
  archived_at DATETIME(6) NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT pk_categories PRIMARY KEY (id),
  CONSTRAINT chk_categories_type CHECK (type IN ('INCOME', 'EXPENSE')),
  CONSTRAINT chk_categories_name_not_blank CHECK (CHAR_LENGTH(TRIM(name)) >= 1),
  CONSTRAINT chk_categories_normalized_name_not_blank
    CHECK (CHAR_LENGTH(TRIM(normalized_name)) >= 1),
  CONSTRAINT uq_categories_type_normalized_name UNIQUE (type, normalized_name)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  DEFAULT COLLATE = utf8mb4_0900_ai_ci;
