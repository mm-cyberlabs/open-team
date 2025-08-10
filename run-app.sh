#!/bin/bash

# Open Team Communication App Launcher
# This script launches the JavaFX application from the built shaded JAR file
# and ensures ~/.openteam/config.yml exists.

set -euo pipefail

# Prefer the shaded JAR
JAR_FILE="$(ls -1 target/open-team-app-*-shaded.jar 2>/dev/null | head -n1)"

echo "Starting Open Team Communication App..."
echo "======================================="

# Build if not present
if [ -z "${JAR_FILE}" ] || [ ! -f "${JAR_FILE}" ]; then
    echo "Shaded JAR not found. Building with 'mvn clean package -DskipTests'..."
    mvn clean package -DskipTests
    JAR_FILE="$(ls -1 target/open-team-app-*-shaded.jar 2>/dev/null | head -n1)"
fi

# Check if JAR file exists
if [ -z "${JAR_FILE}" ] || [ ! -f "${JAR_FILE}" ]; then
    echo "Error: Shaded JAR file not found in target/"
    echo "Please run 'mvn clean package' first to build the application."
    exit 1
fi

# Check Java version
if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java runtime not found on PATH. Please install Java 21+."
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "${JAVA_VERSION}" -lt "21" ]; then
    echo "Error: Java 21 or higher is required. Current version: ${JAVA_VERSION}"
    exit 1
fi

# Ensure ~/.openteam/config.yml exists
OPENTEAM_DIR="${HOME}/.openteam"
CONFIG_FILE="${OPENTEAM_DIR}/config.yml"

mkdir -p "${OPENTEAM_DIR}"
if [ ! -f "${CONFIG_FILE}" ]; then
    cat > "${CONFIG_FILE}" << 'YAML'
# OpenTeam configuration
database:
  url: jdbc:postgresql://localhost:5432/openteam
  username: openteam_user
  password: your_secure_password
  driver: org.postgresql.Driver

application:
  refreshInterval: 10
YAML
    echo "Created starter config at ${CONFIG_FILE}. Please review and update credentials if needed."
fi

# Launch the application
exec java -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -jar "${JAR_FILE}"