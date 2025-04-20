#!/bin/bash

# Check if required arguments are provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <floor_id> <svg_file_path>"
    exit 1
fi

FLOOR_ID=$1
SVG_FILE=$2

# Database connection parameters - match with devcontainer.json
DB_HOST=${POSTGRES_HOST:-db}
DB_USER=${POSTGRES_USER:-postgres}
DB_PASSWORD=${POSTGRES_PASSWORD:-postgres}
DB_NAME=office_management

# Check if the SVG file exists
if [ ! -f "$SVG_FILE" ]; then
    echo "Error: SVG file not found: $SVG_FILE"
    exit 1
fi

# Create a temporary SQL file
TMP_SQL=$(mktemp)

# Base64 encode the SVG content
SVG_BASE64=$(base64 -i "$SVG_FILE")

# Create SQL commands
cat > "$TMP_SQL" << EOF
-- Insert base64 encoded SVG content into temporary table
INSERT INTO temp_svg_loader (svg_content, is_base64) VALUES ('$SVG_BASE64', TRUE);

-- Update the floor planimetry data via the update_floor_plan function
-- This function will handle inserting or updating the floor_planimetry table
SELECT update_floor_plan($FLOOR_ID);
EOF

# Execute the SQL file
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f "$TMP_SQL"

# Clean up
rm "$TMP_SQL"

echo "Floor plan loaded successfully for floor ID: $FLOOR_ID" 