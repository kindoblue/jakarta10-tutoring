#!/bin/bash
set -e

# Build the WAR file
mvn package

# Copy the WAR to the JBoss deployments directory
cp target/office-management-system.war /opt/server/standalone/deployments/ROOT.war

# Start the preinstalled JBoss EAP server
cd /opt/server
bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 