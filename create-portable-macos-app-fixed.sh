#!/bin/bash

# Fixed script to create a fully portable macOS .app bundle for OpenTeam
# This addresses JavaFX module loading issues on macOS

set -e

APP_NAME="OpenTeam"
APP_VERSION="1.0.0"
BUNDLE_ID="com.openteam.app"
MAIN_CLASS="com.openteam.OpenTeamApplication"
MAIN_JAR="open-team-app-${APP_VERSION}-shaded.jar"

echo "🚀 Building Portable OpenTeam macOS Application (FIXED VERSION)..."

# Verify Java 21 is available for building
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "21" ]; then
    echo "❌ Error: Java 21 or higher is required to build. Current version: $JAVA_VERSION"
    exit 1
fi

# Clean and build the project
echo "📦 Building Maven project..."
mvn clean package -DskipTests

# Check if the shaded JAR was created
SHADED_JAR="target/${MAIN_JAR}"
if [ ! -f "$SHADED_JAR" ]; then
    echo "❌ Error: Shaded JAR not found at $SHADED_JAR"
    echo "Please check the Maven build output for errors."
    exit 1
fi

echo "✅ Shaded JAR created successfully: $SHADED_JAR"

# Prepare for jpackage
JPACKAGE_INPUT_DIR="target/jpackage-input"
JPACKAGE_OUTPUT_DIR="target"

echo "📁 Preparing jpackage input directory..."
rm -rf "$JPACKAGE_INPUT_DIR"
mkdir -p "$JPACKAGE_INPUT_DIR"

# Copy the shaded JAR to input directory
cp "$SHADED_JAR" "$JPACKAGE_INPUT_DIR/"

# Prepare app icon (if exists)
ICON_OPTION=""
if [ -f "src/main/resources/icons/openteam-logo.png" ]; then
    echo "🎨 Preparing application icon..."
    ICON_SET_DIR="/tmp/${APP_NAME}.iconset"
    mkdir -p "$ICON_SET_DIR"
    
    # Create different sizes for the iconset
    sips -z 16 16 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_16x16.png" 2>/dev/null || echo "Warning: Could not create 16x16 icon"
    sips -z 32 32 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_16x16@2x.png" 2>/dev/null || echo "Warning: Could not create 32x32 icon"
    sips -z 32 32 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_32x32.png" 2>/dev/null || echo "Warning: Could not create 32x32 icon"
    sips -z 64 64 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_32x32@2x.png" 2>/dev/null || echo "Warning: Could not create 64x64 icon"
    sips -z 128 128 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_128x128.png" 2>/dev/null || echo "Warning: Could not create 128x128 icon"
    sips -z 256 256 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_128x128@2x.png" 2>/dev/null || echo "Warning: Could not create 256x256 icon"
    sips -z 256 256 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_256x256.png" 2>/dev/null || echo "Warning: Could not create 256x256 icon"
    sips -z 512 512 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_256x256@2x.png" 2>/dev/null || echo "Warning: Could not create 512x512 icon"
    sips -z 512 512 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_512x512.png" 2>/dev/null || echo "Warning: Could not create 512x512 icon"
    sips -z 1024 1024 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_512x512@2x.png" 2>/dev/null || echo "Warning: Could not create 1024x1024 icon"
    
    # Create the ICNS file
    ICON_FILE="target/${APP_NAME}.icns"
    iconutil -c icns "$ICON_SET_DIR" --output "$ICON_FILE" 2>/dev/null || echo "Warning: Could not create ICNS file"
    rm -rf "$ICON_SET_DIR"
    
    if [ -f "$ICON_FILE" ]; then
        ICON_OPTION="--icon $ICON_FILE"
    fi
fi

# Clean up any existing app bundle
rm -rf "${JPACKAGE_OUTPUT_DIR}/${APP_NAME}.app"

echo "🔨 Creating self-contained macOS app with jpackage (with JavaFX fixes)..."

# Use jpackage to create the app bundle with embedded JRE and JavaFX module fixes
jpackage \
  --input "$JPACKAGE_INPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "OpenTeam" \
  --description "OpenTeam Communication Application" \
  --copyright "Copyright © 2024 OpenTeam. All rights reserved." \
  --dest "$JPACKAGE_OUTPUT_DIR" \
  --type app-image \
  $ICON_OPTION \
  --java-options "-Xmx1024m" \
  --java-options "-Dfile.encoding=UTF-8" \
  --java-options "-Djava.awt.headless=false" \
  --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.reflect=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.beans=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.graphics/javafx.stage=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.text=ALL-UNNAMED" \
  --java-options "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED"

APP_DIR="${JPACKAGE_OUTPUT_DIR}/${APP_NAME}.app"

if [ -d "$APP_DIR" ]; then
    echo ""
    echo "🔧 Post-processing app bundle..."
    
    # Create .openteam directory structure for logs
    echo "📁 Ensuring ~/.openteam/logs directory exists..."
    mkdir -p ~/.openteam/logs
    
    # Remove quarantine attributes that might prevent the app from launching
    echo "🔓 Removing quarantine attributes..."
    xattr -cr "$APP_DIR" 2>/dev/null || echo "Note: Could not remove quarantine attributes (this is normal)"
    
    # Fix executable permissions
    echo "🔑 Setting executable permissions..."
    chmod +x "$APP_DIR/Contents/MacOS/OpenTeam"
    
    echo ""
    echo "🎉 Portable macOS Application Bundle created successfully!"
    echo "📍 Location: $APP_DIR"
    echo ""
    echo "✨ This app is completely self-contained and includes:"
    echo "   • Embedded Java 21 runtime (no Java installation needed on target machines)"
    echo "   • All application dependencies bundled"
    echo "   • Native macOS launcher"
    echo "   • JavaFX module system fixes for macOS compatibility"
    echo ""
    echo "📝 To test and distribute:"
    echo "1. Test locally: open '$APP_DIR'"
    echo "2. If it doesn't open, run the debug script: ./debug-app.sh"
    echo "3. Copy to Applications: cp -r '$APP_DIR' /Applications/"
    echo "4. Create ZIP for distribution: cd target && zip -r ${APP_NAME}-${APP_VERSION}-macOS-portable.zip ${APP_NAME}.app"
    echo ""
    echo "🔧 User requirements:"
    echo "   • macOS 10.15+ (Catalina or later)"
    echo "   • Configuration file: ~/.openteam/config.yml"
    echo "   • Database connection (PostgreSQL)"
    echo ""
    echo "✅ Build completed successfully!"
    
    # Test the app bundle quickly
    echo ""
    echo "🧪 Quick launch test..."
    timeout 5s "$APP_DIR/Contents/MacOS/OpenTeam" >/dev/null 2>&1 && echo "✅ App launched successfully!" || echo "⚠️  App may have launch issues - run ./debug-app.sh for details"
    
else
    echo "❌ Error: App bundle was not created. Check jpackage output above for errors."
    exit 1
fi
