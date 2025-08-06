#!/bin/bash

echo "🧪 Testing the new fixed build script..."

# Kill any existing build processes
pkill -f "create-portable-macos-app"

# Wait a moment
sleep 2

# Run the new fixed script
echo "Running ./create-portable-macos-app-fixed.sh"
./create-portable-macos-app-fixed.sh

echo "Build completed. Running debug script..."
./debug-app.sh
