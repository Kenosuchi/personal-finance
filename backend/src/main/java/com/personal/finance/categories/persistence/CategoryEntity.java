package com.personal.finance.categories.persistence;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.personal.finance.categories.domain.CategoryType;

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
@Table(name = "categories")
public class CategoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 10)
  private CategoryType type;

  @Column(name = "name", nullable = false, length = 80)
  private String name;

  @Column(name = "normalized_name", nullable = false, length = 80)
  private String normalizedName;

  @Column(name = "archived_at")
  private Instant archivedAt;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected CategoryEntity() {
  }

  public CategoryEntity(CategoryType type, String name, String normalizedName) {
    this.type = type;
    this.name = name;
    this.normalizedName = normalizedName;
  }

  public Long getId() {
    return id;
  }

  public CategoryType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getNormalizedName() {
    return normalizedName;
  }

  public Instant getArchivedAt() {
    return archivedAt;
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
