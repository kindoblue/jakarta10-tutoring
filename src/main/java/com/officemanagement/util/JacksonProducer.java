package com.officemanagement.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import jakarta.enterprise.context.ApplicationScoped; // Removed unused import
import jakarta.enterprise.inject.Produces;

/** CDI Producer for Jackson ObjectMapper. */
// @ApplicationScoped // Removed class-level scope
public class JacksonProducer {

    @Produces
    // @ApplicationScoped // Removed - ObjectMapper not proxyable, use default @Dependent scope
    public ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Optional: Configure the mapper, e.g., register JavaTimeModule for date/time types
        mapper.registerModule(new JavaTimeModule());
        // Add other configurations as needed
        return mapper;
    }
}
