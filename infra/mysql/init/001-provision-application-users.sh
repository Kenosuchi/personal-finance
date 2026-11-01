#!/usr/bin/env bash
set -Eeuo pipefail

required_settings=(
  MYSQL_ROOT_PASSWORD
  MYSQL_DATABASE
  FINANCE_DATABASE_RUNTIME_USERNAME
  FINANCE_DATABASE_RUNTIME_PASSWORD
  FINANCE_DATABASE_MIGRATION_USERNAME
  FINANCE_DATABASE_MIGRATION_PASSWORD
)

for setting in "${required_settings[@]}"; do
  if [[ -z "${!setting:-}" ]]; then
    echo "Required database initialization setting is missing: ${setting}" >&2
    exit 1
  fi
done

for identifier_setting in \
  MYSQL_DATABASE \
  FINANCE_DATABASE_RUNTIME_USERNAME \
  FINANCE_DATABASE_MIGRATION_USERNAME; do
  if [[ ! "${!identifier_setting}" =~ ^[A-Za-z0-9_]+$ ]]; then
    echo "Database initialization setting must contain only letters, digits, or underscore: ${identifier_setting}" >&2
    exit 1
  fi
done

escape_sql_string() {
  local value=$1
  value=${value//\\/\\\\}
  value=${value//\'/\'\'}
  printf '%s' "${value}"
}

runtime_password=$(escape_sql_string "${FINANCE_DATABASE_RUNTIME_PASSWORD}")
migration_password=$(escape_sql_string "${FINANCE_DATABASE_MIGRATION_PASSWORD}")

MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql --protocol=socket -u root <<SQL
CREATE USER IF NOT EXISTS '${FINANCE_DATABASE_RUNTIME_USERNAME}'@'%'
  IDENTIFIED BY '${runtime_password}';
ALTER USER '${FINANCE_DATABASE_RUNTIME_USERNAME}'@'%'
  IDENTIFIED BY '${runtime_password}';
GRANT SELECT, INSERT, UPDATE, DELETE
  ON \`${MYSQL_DATABASE}\`.*
  TO '${FINANCE_DATABASE_RUNTIME_USERNAME}'@'%';

CREATE USER IF NOT EXISTS '${FINANCE_DATABASE_MIGRATION_USERNAME}'@'%'
  IDENTIFIED BY '${migration_password}';
ALTER USER '${FINANCE_DATABASE_MIGRATION_USERNAME}'@'%'
  IDENTIFIED BY '${migration_password}';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES
  ON \`${MYSQL_DATABASE}\`.*
  TO '${FINANCE_DATABASE_MIGRATION_USERNAME}'@'%';
SQL
