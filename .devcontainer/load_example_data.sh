#!/bin/bash

# Wait for PostgreSQL to be ready
until pg_isready -h db -U postgres; do 
  echo "Waiting for PostgreSQL to be ready..."
  sleep 1
done

# First, run the schema.sql to set up the database
echo "Running schema.sql..."
PGPASSWORD=postgres psql -h db -U postgres -f .devcontainer/schema.sql

# Wait a moment to ensure all schema changes are applied
sleep 2

# Load the example floor plan for the first floor (ID 1)
echo "Loading example floor plans..."
.devcontainer/load_floor_plan.sh 1 .devcontainer/svg_floor_plans/floor_1.svg
.devcontainer/load_floor_plan.sh 2 .devcontainer/svg_floor_plans/floor_2.svg
.devcontainer/load_floor_plan.sh 7 .devcontainer/svg_floor_plans/floor_7.svg
.devcontainer/load_floor_plan.sh 8 .devcontainer/svg_floor_plans/floor_8.svg
.devcontainer/load_floor_plan.sh 9 .devcontainer/svg_floor_plans/floor_9.svg

echo "Floor plan loading complete!" 