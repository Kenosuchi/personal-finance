CREATE TABLE transactions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(10) NOT NULL,
  amount_vnd DECIMAL(19, 0) NOT NULL,
  transaction_date DATE NOT NULL,
  category_id BIGINT NOT NULL,
  description VARCHAR(1000) NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT pk_transactions PRIMARY KEY (id),
  CONSTRAINT chk_transactions_type CHECK (type IN ('INCOME', 'EXPENSE')),
  CONSTRAINT chk_transactions_amount
    CHECK (amount_vnd BETWEEN 1 AND 999999999999),
  CONSTRAINT chk_transactions_expense_description
    CHECK (
      type <> 'EXPENSE'
      OR (description IS NOT NULL AND CHAR_LENGTH(TRIM(description)) >= 1)
    ),
  CONSTRAINT fk_transactions_category
    FOREIGN KEY (category_id) REFERENCES categories (id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  INDEX idx_transactions_date_id (transaction_date DESC, id DESC),
  INDEX idx_transactions_category_date_id (category_id, transaction_date DESC, id DESC)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  DEFAULT COLLATE = utf8mb4_0900_ai_ci;
