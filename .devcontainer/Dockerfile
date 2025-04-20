FROM kindoblue/rhjb8:1.0.0


###########################################################
## Install software
USER root

# Create the necessary directory and set permissions
RUN mkdir -p /var/cache/yum/metadata && \
    chmod -R 755 /var/cache/yum

RUN microdnf update -y && \
    microdnf install -y \
      procps-ng \
      sudo \
      shadow-utils \
      maven \
      git \
      postgresql \
      tar \
      diffutils && \
    microdnf clean all && \
    # Allow jboss user to run specific commands via sudo without password
    # Use the correct path for chown, likely /usr/bin/chown
    echo 'jboss ALL=(ALL) NOPASSWD: /usr/sbin/usermod, /usr/sbin/groupmod, /usr/bin/chown, /usr/bin/chmod' > /etc/sudoers.d/devcontainer-jboss && \
    chmod 0440 /etc/sudoers.d/devcontainer-jboss


# Change jboss user shell to /bin/bash (the default shell is /sbin/nologin)
RUN usermod --shell /bin/bash jboss

# Switch back to the default user
USER jboss

# Ensure appropriate permissions for the copied files.
RUN chmod -R ug+rwX $JBOSS_HOME

# Copy custom configuration 
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

CMD ["sh", "-c", "$JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 --debug *:8787"]




