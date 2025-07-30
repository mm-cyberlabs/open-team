#!/bin/bash

# Open Team Communication App Launcher
# This script launches the JavaFX application from the built JAR file

JAR_FILE="target/open-team-app-1.0.0.jar"

echo "Starting Open Team Communication App..."
echo "======================================="

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please run 'mvn clean package' first to build the application."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "21" ]; then
    echo "Error: Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

# Launch the application
java -jar "$JAR_FILE"

echo "Application closed."