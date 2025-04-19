package com.officemanagement.resource;

import com.officemanagement.util.EntityManagerProducer.TestDatabase;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for resource integration tests using Arquillian and a managed EAP instance. Provides
 * common setup like EntityManager injection and deployment creation.
 */
@ExtendWith(ArquillianExtension.class)
public abstract class BaseResourceTest {

    @Inject @TestDatabase EntityManager entityManager;

    @Inject UserTransaction userTransaction;

    /**
     * Creates the deployment archive (WAR) that will be deployed to the EAP instance for testing.
     * This needs to include all necessary application classes, resources, and configuration.
     */
    @Deployment
    public static WebArchive createDeployment() {
        // Create a datasource configuration file
        String dsXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<datasources xmlns=\"http://www.jboss.org/ironjacamar/schema\"\n"
                        + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                        + "    xsi:schemaLocation=\"http://www.jboss.org/ironjacamar/schema http://docs.jboss.org/ironjacamar/schema/datasources_1_0.xsd\">\n"
                        + "    <datasource jndi-name=\"java:jboss/datasources/TestDS\"\n"
                        + "                pool-name=\"TestDS\"\n"
                        + "                enabled=\"true\"\n"
                        + "                use-java-context=\"true\">\n"
                        + "        <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>\n"
                        + "        <driver>h2</driver>\n"
                        + "        <security>\n"
                        + "            <user-name>sa</user-name>\n"
                        + "            <password>sa</password>\n"
                        + "        </security>\n"
                        + "    </datasource>\n"
                        + "</datasources>";

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "com.officemanagement")
                .addClass(com.officemanagement.util.JacksonProducer.class)
                .addClass(com.officemanagement.util.EntityManagerProducer.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource("META-INF/beans.xml", "beans.xml")
                // Add the datasource configuration
                .addAsWebInfResource(new StringAsset(dsXml), "test-ds.xml")
                .addAsLibraries(
                        Maven.resolver()
                                .loadPomFromFile("pom.xml")
                                .resolve(
                                        "com.h2database:h2",
                                        "io.rest-assured:rest-assured",
                                        "org.hamcrest:hamcrest-core",
                                        "io.swagger.core.v3:swagger-core-jakarta",
                                        "io.swagger.core.v3:swagger-jaxrs2-jakarta")
                                .withTransitivity()
                                .asFile());
    }

    /**
     * Database cleanup method using container-managed transactions The @Transactional annotation
     * ensures all operations are performed in a transaction
     */
    @BeforeEach
    public void setupTestDatabase() {
        try {
            userTransaction.begin();
            // Delete all data - order matters for referential integrity
            entityManager.createQuery("DELETE FROM Employee e").executeUpdate();
            entityManager.createQuery("DELETE FROM Seat s").executeUpdate();
            entityManager.createQuery("DELETE FROM OfficeRoom r").executeUpdate();
            entityManager.createQuery("DELETE FROM FloorPlanimetry fp").executeUpdate();
            entityManager.createQuery("DELETE FROM Floor f").executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception ex) {
                throw new RuntimeException("Error rolling back transaction", ex);
            }
            throw new RuntimeException("Error in database setup", e);
        } finally {
            entityManager.clear();
        }
    }

    /** Utility method to convert HTTP status code to Response.Status */
    protected Response.Status getStatus(int statusCode) {
        return Response.Status.fromStatusCode(statusCode);
    }

    /** Interface for operations that might throw checked exceptions. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Executes the provided operation within a transaction boundary. Handles transaction
     * begin/commit/rollback and entity manager clearing.
     *
     * @param operation The operation to execute inside a transaction
     * @throws RuntimeException if the operation fails
     */
    protected void runInTransaction(ThrowingRunnable operation) {
        try {
            userTransaction.begin();
            operation.run();
            userTransaction.commit();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception ex) {
                throw new RuntimeException("Error rolling back transaction", ex);
            }
            throw new RuntimeException("Error in transaction operation", e);
        } finally {
            entityManager.clear();
        }
    }
}
