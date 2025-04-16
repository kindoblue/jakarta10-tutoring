package com.officemanagement.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Qualifier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** CDI Producer for EntityManager for the test persistence unit. */
@ApplicationScoped
public class EntityManagerProducer {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    public @interface TestDatabase {}

    // Inject the test persistence unit defined in persistence.xml
    @PersistenceContext(unitName = "primary")
    private EntityManager entityManager;

    @Produces
    @TestDatabase
    public EntityManager produceTestEntityManager() {
        return entityManager;
    }

    @Produces
    public EntityManager produceEntityManager() {
        return entityManager;
    }
}
