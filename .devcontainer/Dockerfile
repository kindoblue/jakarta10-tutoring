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

# Steps to add:
# (1) COPY the WAR/EAR to $JBOSS_HOME/standalone/deployments
#       with the jboss:root user. For example:
#     COPY --chown=jboss:root my-app.war $JBOSS_HOME/standalone/deployments 3
# (2) (optional) server modification. You can modify EAP server configuration:
#
#       * invoke management operations. For example
#
#        RUN $JBOSS_HOME/bin/jboss-cli.sh --commands="embed-server,/system-property=Foo:add(value=Bar)"
#
#        First operation must always be embed-server.
#
#       * copy a modified standalone.xml in $JBOSS_HOME/standalone/configuration/
#          for example
#
#      COPY --chown=jboss:root standalone.xml  $JBOSS_HOME/standalone/configuration

# Custom configuration
# COPY --chown=jboss:root conf/standalone.xml $JBOSS_HOME/standalone/configuration/

# COPY --chown=jboss:root conf/oracle/jdbc/main $JBOSS_HOME/modules/system/layers/base/com/oracle/jdbc/main/

#COPY --chown=jboss:root target/imageMan $JBOSS_HOME/standalone/deployments/imageMan

# Add admin user
RUN $JBOSS_HOME/bin/add-user.sh admin admin --silent

###########################################################
## Install the procps package to get the ps command (start)
USER root

# Create the necessary directory and set permissions
RUN mkdir -p /var/cache/yum/metadata && \
    chmod -R 755 /var/cache/yum

# Install the procps-ng package to get the ps command
RUN microdnf update -y && \
    microdnf install -y procps-ng && \
    microdnf clean all

# Switch back to the default user
USER jboss

## Install the procps package to get the ps command (end)
###########################################################

###########################################################
## Install Maven (start)
USER root

# Install Maven
RUN microdnf update -y && \
    microdnf install -y maven git postgresql tar diffutils && \
    microdnf clean all

# Change jboss user shell to /bin/bash
RUN usermod --shell /bin/bash jboss

# Switch back to the default user
USER jboss
## Install Maven (end)
###########################################################

# Ensure appropriate permissions for the copied files.
RUN chmod -R ug+rwX $JBOSS_HOME

# Copy custom configuration and define PostgreSQL module
COPY --chown=jboss:root conf/standalone.xml $JBOSS_HOME/standalone/configuration/

# Download PostgreSQL JDBC Driver
RUN curl -L -o /tmp/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar

# Create the module directory for Postgres driver
RUN mkdir -p $JBOSS_HOME/modules/org/postgresql/main

# Move the JAR and module.xml into the module directory
RUN mv /tmp/postgresql.jar $JBOSS_HOME/modules/org/postgresql/main/postgresql.jar
COPY --chown=jboss:root conf/module.xml $JBOSS_HOME/modules/org/postgresql/main/module.xml

# Debug: show environment variables and directory contents
RUN echo "JBOSS_HOME=$JBOSS_HOME" && \
    echo "PWD=$(pwd)" && \
    echo "HOME=$HOME" && \
    ls -la $JBOSS_HOME/modules/org/postgresql/main/

# Copy CLI script
COPY --chown=jboss:root conf/add-postgres-datasource.cli $JBOSS_HOME/add-postgres-datasource.cli

# Run CLI script (module creation, driver registration, datasource creation)
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/add-postgres-datasource.cli

EXPOSE 8080 8787 9990

ENV DB_PASSWORD_SLF=""
ENV DB_PASSWORD_NCF=""
ENV DB_PASSWORD_OPK=""
ENV DB_PASSWORD_LIB=""
ENV DB_PASSWORD_NSC=""

# Run the JBoss EAP 8 server (in standalone mode) with the default configuration, with debug port enabled (8787)
#CMD ["$JBOSS_HOME/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "--debug", "*:8787"]
CMD ["sh", "-c", "$JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 --debug *:8787"]




