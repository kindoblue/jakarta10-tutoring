#!/bin/bash
set -e

# Build the WAR file
mvn package

# Copy the WAR to the JBoss deployments directory
cp target/office-management-system.war /opt/server/standalone/deployments/ROOT.war

# Clean up the history directory to avoid boot errors
rm -rf /opt/server/standalone/configuration/standalone_xml_history/current/* || true

# Start the preinstalled JBoss EAP server
cd /opt/server
bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 -Dorg.jboss.server.config.history.disabled=true -Djboss.config.current-history-length=0 -Djboss.config.history-days=0 