#!/bin/bash

# Debug script for OpenTeam macOS app issues
# This script will help diagnose why the app isn't starting and logs aren't being created

echo "🔍 Debugging OpenTeam macOS App Launch Issues"
echo "=============================================="

APP_PATH="target/OpenTeam.app"

# Check if app bundle exists
if [ ! -d "$APP_PATH" ]; then
    echo "❌ App bundle not found at $APP_PATH"
    echo "Please run ./create-portable-macos-app.sh first"
    exit 1
fi

echo "✅ App bundle found at $APP_PATH"

# Check app bundle structure
echo "📂 App bundle structure:"
find "$APP_PATH" -type f | head -20

echo ""
echo "📋 Info.plist contents:"
if [ -f "$APP_PATH/Contents/Info.plist" ]; then
    plutil -p "$APP_PATH/Contents/Info.plist"
else
    echo "❌ Info.plist not found"
fi

echo ""
echo "⚙️  App configuration file (OpenTeam.cfg):"
if [ -f "$APP_PATH/Contents/app/OpenTeam.cfg" ]; then
    cat "$APP_PATH/Contents/app/OpenTeam.cfg"
else
    echo "❌ OpenTeam.cfg not found"
fi

echo ""
echo "🔧 Executable permissions:"
ls -la "$APP_PATH/Contents/MacOS/"

echo ""
echo "☕ Java runtime check:"
if [ -d "$APP_PATH/Contents/runtime" ]; then
    echo "✅ Embedded runtime found"
    find "$APP_PATH/Contents/runtime" -name "java" -type f
else
    echo "❌ No embedded runtime found"
fi

echo ""
echo "📦 JAR file check:"
ls -la "$APP_PATH/Contents/app/"

echo ""
echo "🏠 Creating ~/.openteam directory if it doesn't exist:"
mkdir -p ~/.openteam/logs
echo "✅ ~/.openteam/logs directory created"

echo ""
echo "🧪 Testing direct JAR execution:"
echo "Running: java -jar $APP_PATH/Contents/app/open-team-app-1.0.0-shaded.jar"
echo "(This will help identify Java/JavaFX issues)"

# Try to run the JAR directly to see what errors occur
cd "$(dirname "$APP_PATH")"
timeout 10s java -jar "$APP_PATH/Contents/app/open-team-app-1.0.0-shaded.jar" 2>&1 | head -20

echo ""
echo "📱 Testing app launcher directly:"
echo "Running: $APP_PATH/Contents/MacOS/OpenTeam"

# Try to run the app launcher directly
timeout 10s "$APP_PATH/Contents/MacOS/OpenTeam" 2>&1 | head -20

echo ""
echo "📋 System console logs (last 50 lines mentioning OpenTeam or Java):"
log show --last 5m --predicate 'process CONTAINS "OpenTeam" OR process CONTAINS "java"' 2>/dev/null | tail -50

echo ""
echo "🔍 Check for crash reports:"
ls -la ~/Library/Logs/DiagnosticReports/*OpenTeam* 2>/dev/null || echo "No crash reports found"

echo ""
echo "📝 Checking for any logs in ~/.openteam:"
ls -la ~/.openteam/ 2>/dev/null || echo "~/.openteam directory is empty or doesn't exist"

echo ""
echo "🏥 macOS app notarization and quarantine check:"
xattr "$APP_PATH"

echo ""
echo "🔍 Debug Summary:"
echo "- If the app bundle is properly created but won't launch, it's likely a JavaFX module issue"
echo "- Check the Java runtime execution output above for module path errors"
echo "- The shaded JAR warnings about module-info.class suggest module system conflicts"
echo "- If no errors appear above, the issue might be macOS security restrictions"

echo ""
echo "💡 Recommended fixes:"
echo "1. Try: xattr -dr com.apple.quarantine $APP_PATH"
echo "2. Add --add-exports and --add-opens JVM arguments for JavaFX modules"
echo "3. Check JavaFX module path configuration in jpackage"
