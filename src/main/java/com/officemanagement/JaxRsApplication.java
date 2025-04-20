package com.officemanagement;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationPath("/api")
public class JaxRsApplication extends Application {

    public JaxRsApplication() {
        super();
        OpenAPI oas = new OpenAPI();
        Info info =
                new Info()
                        .title("Office Management API")
                        .version("1.0.0")
                        .description(
                                "API for managing office floors, rooms, seats, and employees.");
        oas.info(info);
    }

    @Override
    public Set<Class<?>> getClasses() {
        // Add your resource classes and the OpenApiResource
        Set<Class<?>> resources =
                Stream.of(
                                com.officemanagement.resource.EmployeeResource.class,
                                com.officemanagement.resource.FloorResource.class,
                                com.officemanagement.resource.RoomResource.class,
                                com.officemanagement.resource.SeatResource.class,
                                com.officemanagement.resource.StatsResource.class,
                                OpenApiResource.class // Add Swagger's JAX-RS resource
                                )
                        .collect(Collectors.toSet());
        return resources;
    }

    // If you use singletons (like @ApplicationScoped beans),
    // you might need to override getSingletons() as well.
}
