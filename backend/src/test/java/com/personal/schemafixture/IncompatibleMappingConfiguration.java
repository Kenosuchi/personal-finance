package com.personal.schemafixture;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;

import com.personal.finance.categories.persistence.CategoryEntity;
import com.personal.finance.transactions.persistence.TransactionEntity;

@TestConfiguration(proxyBeanMethods = false)
@EntityScan(basePackageClasses = {
    CategoryEntity.class,
    TransactionEntity.class,
    IncompatibleTransactionEntity.class
})
public class IncompatibleMappingConfiguration {
}
