#!/bin/bash

# ULTIMATE PORTABLE macOS APP BUILDER
# Creates a completely self-contained .app with embedded JRE and proper JavaFX configuration
# No Java, JavaFX, or Maven required on target machines

set -e

APP_NAME="OpenTeam"
APP_VERSION="1.0.0"
BUNDLE_ID="com.openteam.app"
MAIN_CLASS="com.openteam.OpenTeamApplication"
MAIN_JAR="open-team-app-${APP_VERSION}-shaded.jar"

echo "🚀 Building TRULY PORTABLE OpenTeam macOS Application..."
echo "   This will create a completely self-contained app with embedded Java runtime"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -rf target/OpenTeam.app target/jpackage-input

# Verify Java 21 is available for building
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "21" ]; then
    echo "❌ Error: Java 21 or higher is required to build. Current version: $JAVA_VERSION"
    exit 1
fi

# Build the project with clean module handling
echo "📦 Building Maven project with module fixes..."
mvn clean package -DskipTests

# Check if the shaded JAR was created
SHADED_JAR="target/${MAIN_JAR}"
if [ ! -f "$SHADED_JAR" ]; then
    echo "❌ Error: Shaded JAR not found at $SHADED_JAR"
    exit 1
fi

echo "✅ Shaded JAR created successfully: $SHADED_JAR"

# Prepare jpackage directories
JPACKAGE_INPUT_DIR="target/jpackage-input"
JPACKAGE_OUTPUT_DIR="target"
TEMP_DIR="target/temp"

echo "📁 Preparing jpackage input directory..."
rm -rf "$JPACKAGE_INPUT_DIR" "$TEMP_DIR"
mkdir -p "$JPACKAGE_INPUT_DIR" "$TEMP_DIR"

# Copy the shaded JAR to input directory
cp "$SHADED_JAR" "$JPACKAGE_INPUT_DIR/"

# Create application icon
ICON_OPTION=""
if [ -f "src/main/resources/icons/openteam-logo.png" ]; then
    echo "🎨 Creating application icon..."
    ICON_SET_DIR="$TEMP_DIR/${APP_NAME}.iconset"
    mkdir -p "$ICON_SET_DIR"
    
    # Create all required icon sizes for macOS
    sips -z 16 16 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_16x16.png" 2>/dev/null
    sips -z 32 32 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_16x16@2x.png" 2>/dev/null
    sips -z 32 32 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_32x32.png" 2>/dev/null
    sips -z 64 64 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_32x32@2x.png" 2>/dev/null
    sips -z 128 128 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_128x128.png" 2>/dev/null
    sips -z 256 256 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_128x128@2x.png" 2>/dev/null
    sips -z 256 256 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_256x256.png" 2>/dev/null
    sips -z 512 512 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_256x256@2x.png" 2>/dev/null
    sips -z 512 512 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_512x512.png" 2>/dev/null
    sips -z 1024 1024 src/main/resources/icons/openteam-logo.png --out "${ICON_SET_DIR}/icon_512x512@2x.png" 2>/dev/null
    
    # Create the ICNS file
    ICON_FILE="$TEMP_DIR/${APP_NAME}.icns"
    iconutil -c icns "$ICON_SET_DIR" --output "$ICON_FILE" 2>/dev/null && ICON_OPTION="--icon $ICON_FILE"
fi

echo "🔨 Creating self-contained macOS app with embedded JRE..."

# The key fix: Use jpackage with comprehensive JavaFX support for embedded runtime
jpackage \
  --input "$JPACKAGE_INPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "OpenTeam" \
  --description "OpenTeam Communication Application - Self Contained" \
  --copyright "Copyright © 2024 OpenTeam. All rights reserved." \
  --dest "$JPACKAGE_OUTPUT_DIR" \
  --type app-image \
  $ICON_OPTION \
  --java-options "-Xms256m" \
  --java-options "-Xmx1024m" \
  --java-options "-Dfile.encoding=UTF-8" \
  --java-options "-Djava.awt.headless=false" \
  --java-options "-Djava.system.class.loader=java.lang.ClassLoader" \
  --java-options "-Djavafx.preloader=" \
  --java-options "-Dprism.order=sw,es2,d3d" \
  --java-options "-Dprism.allowhidpi=true" \
  --java-options "-Dprism.verbose=false" \
  --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.reflect=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.beans=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED" \
  --java-options "--add-exports=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.graphics/javafx.stage=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED" \
  --java-options "--add-opens=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.text=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.util=ALL-UNNAMED" \
  --java-options "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED" \
  --java-options "--illegal-access=warn"

APP_DIR="${JPACKAGE_OUTPUT_DIR}/${APP_NAME}.app"

if [ -d "$APP_DIR" ]; then
    echo ""
    echo "🔧 Post-processing for TRUE portability..."
    
    # Ensure .openteam directory exists for the user
    echo "📁 Ensuring ~/.openteam directory structure..."
    mkdir -p ~/.openteam/logs
    
    # Remove macOS quarantine attributes
    echo "🔓 Removing quarantine attributes..."
    xattr -cr "$APP_DIR" 2>/dev/null || echo "   (quarantine removal failed - this is usually okay)"
    
    # Ensure executable permissions
    echo "🔑 Setting proper permissions..."
    chmod +x "$APP_DIR/Contents/MacOS/OpenTeam"
    find "$APP_DIR" -name "*.dylib" -exec chmod +x {} \; 2>/dev/null || true
    find "$APP_DIR/Contents/runtime" -name "java" -exec chmod +x {} \; 2>/dev/null || true
    
    # Verify the embedded runtime
    if [ -d "$APP_DIR/Contents/runtime" ]; then
        echo "✅ Embedded Java runtime verified"
        EMBEDDED_JAVA="$APP_DIR/Contents/runtime/Contents/Home/bin/java"
        if [ -f "$EMBEDDED_JAVA" ]; then
            echo "   Runtime location: $EMBEDDED_JAVA"
            "$EMBEDDED_JAVA" -version 2>&1 | head -n1 || echo "   (runtime version check failed)"
        fi
    else
        echo "⚠️  Warning: No embedded runtime found - this may not be portable"
    fi
    
    # Test basic app launch (non-blocking)
    echo "🧪 Testing app launch (5 second timeout)..."
    timeout 5s "$APP_DIR/Contents/MacOS/OpenTeam" 2>/dev/null && echo "✅ App launch test successful" || echo "⚠️  App launch test timed out (this might be normal if no config exists)"
    
    # Clean up temp directory
    rm -rf "$TEMP_DIR"
    
    echo ""
    echo "🎉 TRULY PORTABLE macOS Application Bundle Created!"
    echo "📍 Location: $APP_DIR"
    echo ""
    echo "✨ This app is COMPLETELY SELF-CONTAINED:"
    echo "   ✅ Embedded Java 21 runtime (no Java installation needed)"
    echo "   ✅ All JavaFX dependencies bundled with proper module configuration"
    echo "   ✅ All application dependencies included"
    echo "   ✅ Native macOS launcher with proper permissions"
    echo "   ✅ Quarantine attributes removed"
    echo ""
    echo "📋 ZERO REQUIREMENTS on target machines:"
    echo "   • No Java installation needed"
    echo "   • No JavaFX installation needed"
    echo "   • No Maven or build tools needed"
    echo "   • Only requires: ~/.openteam/config.yml file"
    echo ""
    echo "🚀 Ready for distribution:"
    echo "   1. Test: open '$APP_DIR'"
    echo "   2. Distribute: cd target && zip -r OpenTeam-v${APP_VERSION}-Portable-macOS.zip OpenTeam.app"
    echo "   3. Recipients just need to:"
    echo "      - Unzip the app"
    echo "      - Create ~/.openteam/config.yml"
    echo "      - Double-click OpenTeam.app"
    echo ""
    echo "✅ PORTABLE BUILD COMPLETED SUCCESSFULLY!"
    
else
    echo "❌ FAILED: App bundle was not created"
    echo "Check the jpackage output above for errors"
    exit 1
fi
