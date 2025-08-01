#!/bin/bash

# Script to create a macOS .app bundle for OpenTeam
# This script packages the JavaFX application as a native macOS application

set -e

APP_NAME="OpenTeam"
APP_VERSION="1.0.0"
BUNDLE_ID="com.openteam.app"
MAIN_CLASS="com.openteam.OpenTeamApplication"

echo "🚀 Building OpenTeam macOS Application..."

# Clean and build the project
echo "📦 Building Maven project..."
mvn clean package -DskipTests

# Check if the shaded JAR was created
SHADED_JAR="target/open-team-app-${APP_VERSION}-shaded.jar"
if [ ! -f "$SHADED_JAR" ]; then
    echo "❌ Error: Shaded JAR not found at $SHADED_JAR"
    echo "Please check the Maven build output for errors."
    exit 1
fi

echo "✅ Shaded JAR created successfully: $SHADED_JAR"

# Create the app bundle directory structure
APP_DIR="target/${APP_NAME}.app"
CONTENTS_DIR="${APP_DIR}/Contents"
MACOS_DIR="${CONTENTS_DIR}/MacOS"
RESOURCES_DIR="${CONTENTS_DIR}/Resources"
JAVA_DIR="${CONTENTS_DIR}/Java"

echo "📁 Creating app bundle structure..."
rm -rf "$APP_DIR"
mkdir -p "$MACOS_DIR" "$RESOURCES_DIR" "$JAVA_DIR"

# Copy the JAR file
echo "📋 Copying JAR file..."
cp "$SHADED_JAR" "${JAVA_DIR}/${APP_NAME}.jar"

# Copy the icon if it exists
if [ -f "src/main/resources/icons/openteam-logo.png" ]; then
    echo "🎨 Copying application icon..."
    # Convert PNG to ICNS for macOS (requires iconutil, which is built into macOS)
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
    iconutil -c icns "$ICON_SET_DIR" --output "${RESOURCES_DIR}/${APP_NAME}.icns" 2>/dev/null || echo "Warning: Could not create ICNS file"
    rm -rf "$ICON_SET_DIR"
else
    echo "⚠️  Warning: No icon found at src/main/resources/icons/openteam-logo.png"
fi

# Create the launcher script
echo "📝 Creating launcher script..."
cat > "${MACOS_DIR}/${APP_NAME}" << EOF
#!/bin/bash

# Get the directory where this script is located
DIR="\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )" && pwd )"
APP_ROOT="\$DIR/.."

# Set up Java options
JAVA_OPTS="-Xmx1024m -Dfile.encoding=UTF-8 -Djava.awt.headless=false"

# Add macOS specific options
JAVA_OPTS="\$JAVA_OPTS -Xdock:name=${APP_NAME}"
if [ -f "\$APP_ROOT/Resources/${APP_NAME}.icns" ]; then
    JAVA_OPTS="\$JAVA_OPTS -Xdock:icon=\$APP_ROOT/Resources/${APP_NAME}.icns"
fi

# Launch the application
exec java \$JAVA_OPTS -jar "\$APP_ROOT/Java/${APP_NAME}.jar" "\$@"
EOF

chmod +x "${MACOS_DIR}/${APP_NAME}"

# Create Info.plist
echo "📄 Creating Info.plist..."
cat > "${CONTENTS_DIR}/Info.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>en</string>
    <key>CFBundleExecutable</key>
    <string>${APP_NAME}</string>
    <key>CFBundleIconFile</key>
    <string>${APP_NAME}.icns</string>
    <key>CFBundleIdentifier</key>
    <string>${BUNDLE_ID}</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>${APP_NAME}</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>${APP_VERSION}</string>
    <key>CFBundleVersion</key>
    <string>${APP_VERSION}</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.15</string>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.productivity</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>NSSupportsAutomaticGraphicsSwitching</key>
    <true/>
    <key>CFBundleDisplayName</key>
    <string>Open Team</string>
    <key>CFBundleGetInfoString</key>
    <string>Open Team Communication App ${APP_VERSION}</string>
    <key>NSHumanReadableCopyright</key>
    <string>Copyright © 2024 Open Team. All rights reserved.</string>
</dict>
</plist>
EOF

# Create PkgInfo
echo "📋 Creating PkgInfo..."
echo "APPL????" > "${CONTENTS_DIR}/PkgInfo"

echo ""
echo "🎉 macOS Application Bundle created successfully!"
echo "📍 Location: $APP_DIR"
echo ""
echo "📝 Next steps:"
echo "1. Test the app by double-clicking: $APP_DIR"
echo "2. Copy to Applications folder: cp -r '$APP_DIR' /Applications/"
echo "3. Or drag and drop '$APP_NAME.app' to /Applications in Finder"
echo ""
echo "🔧 Troubleshooting:"
echo "- If the app doesn't start, check Console.app for error messages"
echo "- Make sure PostgreSQL is running and accessible"
echo "- Verify the config file exists at ~/.openteam/config.yml"
echo ""
echo "✅ Build completed successfully!"