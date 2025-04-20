# https://docs.redhat.com/en/documentation/red_hat_jboss_enterprise_application_platform/8.0/html/using_jboss_eap_on_openshift_container_platform/assembly_building-and-running-jboss-eap-applicationson-openshift-container-platform_default#assembly_deploying-a-third-party-application-on-openshift_assembly_building-and-running-jboss-eap-applicationson-openshift-container-platform
# https://github.com/jboss-container-images/jboss-eap-8-openshift-image

# Use EAP 8 Builder image to create a JBoss EAP 8 server
# with its default configuration

FROM registry.redhat.io/jboss-eap-8/eap8-openjdk21-builder-openshift-rhel9:latest AS builder

# Set up environment variables for provisioning.
ENV GALLEON_PROVISION_FEATURE_PACKS org.jboss.eap:wildfly-ee-galleon-pack,org.jboss.eap.cloud:eap-cloud-galleon-pack
ENV GALLEON_PROVISION_LAYERS cloud-default-config
# Specify the JBoss EAP version
ENV GALLEON_PROVISION_CHANNELS org.jboss.eap.channels:eap-8.0

# Run the assemble script to provision the server.
RUN /usr/local/s2i/assemble

# Copy the JBoss EAP 8 server from the builder image to the runtime image.
FROM registry.redhat.io/jboss-eap-8/eap8-openjdk21-runtime-openshift-rhel9:latest AS runtime

# Set appropriate ownership and permissions.
COPY --from=builder --chown=jboss:root $JBOSS_HOME $JBOSS_HOME

# Define JBOSS_HOME explicitly for clarity
ENV JBOSS_HOME=/opt/server

# Add admin user
RUN $JBOSS_HOME/bin/add-user.sh admin admin --silent