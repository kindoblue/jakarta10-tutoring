#!/bin/bash
set -e

# Set required Postgres datasource environment variables for JBoss EAP 8
export POSTGRESQL_USER=postgres
export POSTGRESQL_PASSWORD=postgres
export POSTGRESQL_DATABASE=postgres
export POSTGRESQL_SERVICE_HOST=db

# Start the provisioned JBoss EAP server
cd "$(dirname "$0")/target/server"

# Start the server, binding to all interfaces
bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 