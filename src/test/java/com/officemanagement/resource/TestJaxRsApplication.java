package com.officemanagement.resource;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS application configuration for tests. Registers all the resource classes needed for the
 * embedded server.
 */
@ApplicationPath("/") // Defines the base path for the application
public class TestJaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Register resource classes
        classes.add(EmployeeResource.class);
        classes.add(FloorResource.class);
        classes.add(RoomResource.class);
        classes.add(SeatResource.class);
        classes.add(StatsResource.class);
        // Add other resource classes here if needed
        return classes;
    }

    // If using singletons (e.g., with manual instantiation or specific lifecycle),
    // override getSingletons() instead or in addition.
    // For CDI-managed beans, getClasses() is generally sufficient as Weld/RESTEasy
    // will handle instantiation.
}
