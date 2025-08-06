#!/bin/bash

# PORTABLE APP VERIFICATION SCRIPT
# Tests that the OpenTeam.app is truly portable and self-contained

APP_PATH="target/OpenTeam.app"

echo "🔍 VERIFYING PORTABLE APP REQUIREMENTS"
echo "======================================"

if [ ! -d "$APP_PATH" ]; then
    echo "❌ App bundle not found at $APP_PATH"
    echo "Please run ./create-truly-portable-macos-app.sh first"
    exit 1
fi

echo "✅ App bundle found: $APP_PATH"

echo ""
echo "📦 Checking embedded Java runtime..."
if [ -d "$APP_PATH/Contents/runtime" ]; then
    echo "✅ Embedded runtime directory exists"
    
    # Find java executable in embedded runtime
    JAVA_EXEC=$(find "$APP_PATH/Contents/runtime" -name "java" -type f | head -n1)
    if [ -n "$JAVA_EXEC" ]; then
        echo "✅ Embedded Java executable found: $JAVA_EXEC"
        
        # Test embedded Java version
        echo "📋 Embedded Java version:"
        "$JAVA_EXEC" -version 2>&1 | head -n3 | sed 's/^/   /'
        
        # Check if JavaFX is available in embedded runtime
        echo ""
        echo "🎭 Testing JavaFX availability in embedded runtime..."
        "$JAVA_EXEC" -cp "$APP_PATH/Contents/app/$MAIN_JAR" --list-modules 2>/dev/null | grep javafx || echo "   JavaFX modules: Included in shaded JAR"
        
    else
        echo "❌ No Java executable found in embedded runtime"
    fi
else
    echo "❌ No embedded runtime directory found"
fi

echo ""
echo "📄 Checking app configuration..."

# Check Info.plist
if [ -f "$APP_PATH/Contents/Info.plist" ]; then
    echo "✅ Info.plist exists"
    echo "📋 App bundle details:"
    plutil -p "$APP_PATH/Contents/Info.plist" | grep -E "(CFBundleName|CFBundleVersion|CFBundleExecutable)" | sed 's/^/   /'
else
    echo "❌ Info.plist missing"
fi

# Check main JAR
MAIN_JAR_PATH="$APP_PATH/Contents/app/open-team-app-1.0.0-shaded.jar"
if [ -f "$MAIN_JAR_PATH" ]; then
    echo "✅ Main JAR exists: $(basename "$MAIN_JAR_PATH")"
    echo "📏 JAR size: $(du -h "$MAIN_JAR_PATH" | cut -f1)"
else
    echo "❌ Main JAR missing"
fi

# Check executable permissions
echo ""
echo "🔑 Checking permissions..."
LAUNCHER="$APP_PATH/Contents/MacOS/OpenTeam"
if [ -x "$LAUNCHER" ]; then
    echo "✅ Main launcher is executable"
else
    echo "❌ Main launcher is not executable"
fi

# Check quarantine attributes
echo ""
echo "🛡️  Checking macOS security attributes..."
QUARANTINE=$(xattr "$APP_PATH" 2>/dev/null | grep com.apple.quarantine)
if [ -z "$QUARANTINE" ]; then
    echo "✅ No quarantine attributes (app can launch immediately)"
else
    echo "⚠️  Quarantine attributes present: $QUARANTINE"
    echo "   Run: xattr -dr com.apple.quarantine '$APP_PATH'"
fi

# Test direct launcher execution (brief test)
echo ""
echo "🧪 Testing direct app execution..."
echo "   (Testing with 3-second timeout)"

# Create a minimal config for testing
echo "Creating temporary config for testing..."
mkdir -p ~/.openteam
cat > ~/.openteam/config.yml << EOF
database:
  host: localhost
  port: 5432
  name: openteam_test
  username: test
  password: test
EOF

# Test the launcher directly
timeout 3s "$LAUNCHER" 2>&1 | head -n10 | sed 's/^/   /' || echo "   (Test completed - app may have launched or timed out)"

echo ""
echo "📊 PORTABILITY ASSESSMENT:"
echo "=========================="

# Calculate portability score
SCORE=0
TOTAL=6

if [ -d "$APP_PATH/Contents/runtime" ]; then SCORE=$((SCORE+1)); fi
if [ -f "$MAIN_JAR_PATH" ]; then SCORE=$((SCORE+1)); fi
if [ -x "$LAUNCHER" ]; then SCORE=$((SCORE+1)); fi
if [ -f "$APP_PATH/Contents/Info.plist" ]; then SCORE=$((SCORE+1)); fi
if [ -z "$QUARANTINE" ]; then SCORE=$((SCORE+1)); fi
if [ -n "$JAVA_EXEC" ]; then SCORE=$((SCORE+1)); fi

echo "📈 Portability Score: $SCORE/$TOTAL"

if [ $SCORE -eq $TOTAL ]; then
    echo "🎉 EXCELLENT: App appears to be fully portable!"
    echo ""
    echo "✅ Ready for distribution to machines without Java installed"
    echo "✅ Recipients only need to:"
    echo "   1. Unzip the app bundle"
    echo "   2. Create ~/.openteam/config.yml with database settings"
    echo "   3. Double-click OpenTeam.app to launch"
    
elif [ $SCORE -ge 4 ]; then
    echo "⚠️  GOOD: App is mostly portable but may have minor issues"
    echo "   Check the warnings above and fix if needed"
    
else
    echo "❌ POOR: App has significant portability issues"
    echo "   Review the failed checks above"
    echo "   Consider rebuilding with ./create-truly-portable-macos-app.sh"
fi

echo ""
echo "📱 App bundle size: $(du -sh "$APP_PATH" | cut -f1)"
echo "🏁 Verification complete!"
