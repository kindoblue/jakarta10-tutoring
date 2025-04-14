package com.officemanagement.resource;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for resource integration tests using Arquillian and a managed EAP instance. Provides
 * common setup like EntityManager injection and deployment creation.
 */
@ExtendWith(ArquillianExtension.class) // Use Arquillian JUnit 5 extension
public abstract class BaseResourceTest {

    @Inject // Re-enable EntityManager injection
    EntityManager entityManager;

    /**
     * Creates the deployment archive (WAR) that will be deployed to the EAP instance for testing.
     * This needs to include all necessary application classes, resources, and configuration.
     */
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war") // Name the test deployment
                .addPackages(true, "com.officemanagement") // Add all application packages
                // Add necessary configuration files
                .addAsResource("META-INF/persistence.xml")
                // Add beans.xml for CDI activation. Load it from the test resources META-INF
                // directory.
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
        // Add any other required resources (e.g., web.xml if used)
        ;
    }

    // Restore database cleanup method, using manual transactions
    @BeforeEach
    // @Transactional // Removed as we use RESOURCE_LOCAL
    public void setupTestDatabase() {
        // Manually start transaction
        entityManager.getTransaction().begin();

        try {
            // Delete in reverse order of dependencies to avoid constraint violations
            entityManager.createQuery("DELETE FROM Employee e").executeUpdate();
            entityManager.createQuery("DELETE FROM Seat s").executeUpdate();
            entityManager.createQuery("DELETE FROM OfficeRoom r").executeUpdate();
            entityManager.createQuery("DELETE FROM FloorPlanimetry fp").executeUpdate();
            entityManager.createQuery("DELETE FROM Floor f").executeUpdate();

            // Commit transaction
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            // Rollback if anything went wrong
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e; // Re-throw exception to fail the test setup
        }
    }

    // Utility method to get Response.Status constants
    protected Response.Status getStatus(int statusCode) {
        return Response.Status.fromStatusCode(statusCode);
    }
}
