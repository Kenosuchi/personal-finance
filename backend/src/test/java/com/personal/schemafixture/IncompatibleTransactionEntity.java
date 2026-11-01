package com.personal.schemafixture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "IncompatibleTransactionFixture")
@Table(name = "transactions")
public class IncompatibleTransactionEntity {

  @Id
  private Long id;

  @Column(name = "missing_mapping_probe", nullable = false)
  private String missingMappingProbe;
}
