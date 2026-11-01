package com.personal.finance.transactions.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.personal.finance.transactions.domain.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 10)
  private TransactionType type;

  @Column(name = "amount_vnd", nullable = false, precision = 19, scale = 0)
  private BigDecimal amountVnd;

  @Column(name = "transaction_date", nullable = false)
  private LocalDate transactionDate;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Column(name = "description", length = 1000)
  private String description;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected TransactionEntity() {
  }

  public TransactionEntity(
      TransactionType type,
      BigDecimal amountVnd,
      LocalDate transactionDate,
      Long categoryId,
      String description) {
    this.type = type;
    this.amountVnd = amountVnd;
    this.transactionDate = transactionDate;
    this.categoryId = categoryId;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public TransactionType getType() {
    return type;
  }

  public BigDecimal getAmountVnd() {
    return amountVnd;
  }

  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public String getDescription() {
    return description;
  }

  public long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
