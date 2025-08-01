# 📦 OpenTeam Application Packaging Guide

This guide explains how to package the OpenTeam JavaFX application for macOS distribution.

## 🚀 Quick Start - Create macOS App Bundle

The easiest way to create a native macOS application:

```bash
# 1. Make sure you're in the project root directory
cd /path/to/open-team

# 2. Run the packaging script
./create-macos-app.sh
```

This will create `target/OpenTeam.app` that you can:
- Double-click to run
- Copy to `/Applications/` 
- Distribute to other macOS users

## 📋 Prerequisites

- **Java 21**: Make sure you have Java 21 installed
- **Maven**: For building the project
- **macOS**: This guide is specific to macOS packaging
- **PostgreSQL**: Database should be running for the app to work

## 🛠️ Manual Build Steps

If you want to understand the process or customize it:

### 1. Build the Fat JAR

```bash
mvn clean package -DskipTests
```

This creates:
- `target/open-team-app-1.0.0.jar` - Regular JAR
- `target/open-team-app-1.0.0-shaded.jar` - Fat JAR with all dependencies

### 2. Test the Fat JAR

```bash
java -jar target/open-team-app-1.0.0-shaded.jar
```

### 3. Create macOS App Bundle (Manual)

```bash
# Create app structure
mkdir -p "OpenTeam.app/Contents/MacOS"
mkdir -p "OpenTeam.app/Contents/Resources" 
mkdir -p "OpenTeam.app/Contents/Java"

# Copy JAR
cp target/open-team-app-1.0.0-shaded.jar OpenTeam.app/Contents/Java/OpenTeam.jar

# Create launcher script (see create-macos-app.sh for details)
# Create Info.plist (see create-macos-app.sh for details)
```

## 🎯 Alternative Packaging Methods

### Method 1: JavaFX jpackage (Advanced)

For a truly native app with embedded JRE:

```bash
# This requires the jpackage tool and more complex setup
mvn clean package
mvn jlink:jlink
mvn jpackage:jpackage
```

### Method 2: Simple JAR Distribution

For developers who want to run from command line:

```bash
# Build and run
mvn clean package -DskipTests
java -jar target/open-team-app-1.0.0-shaded.jar
```

### Method 3: Script Wrapper

Create a simple shell script:

```bash
#!/bin/bash
java -Xmx1024m -jar open-team-app-1.0.0-shaded.jar
```

## 📁 App Bundle Structure

The created `.app` bundle has this structure:

```
OpenTeam.app/
├── Contents/
│   ├── Info.plist          # App metadata
│   ├── PkgInfo             # Package type info
│   ├── MacOS/
│   │   └── OpenTeam        # Launcher script
│   ├── Resources/
│   │   └── OpenTeam.icns   # App icon
│   └── Java/
│       └── OpenTeam.jar    # Application JAR
```

## 🔧 Configuration Requirements

Before running the app, users need:

### 1. Database Setup

```bash
# Install PostgreSQL (if not already installed)
brew install postgresql

# Start PostgreSQL
brew services start postgresql

# Run the SQL scripts (in order):
psql -U postgres -f src/main/resources/sql/01-create-database.sql
psql -U postgres -d openteam -f src/main/resources/sql/02-create-schema.sql  
psql -U postgres -d openteam -f src/main/resources/sql/03-create-tables.sql
psql -U postgres -d openteam -f src/main/resources/sql/04-sample-data.sql

# For existing databases, run migration:
psql -U postgres -d openteam -f src/main/resources/sql/05-migrate-activities-to-target-dates.sql
```

### 2. Configuration File

Create `~/.openteam/config.yml`:

```yaml
database:
  url: jdbc:postgresql://localhost:5432/openteam
  username: openteam_user
  password: your_secure_password
  driver: org.postgresql.Driver

application:
  refreshInterval: 10
```

## 🚀 Distribution

### For Personal Use
```bash
# Copy to Applications
cp -r target/OpenTeam.app /Applications/
```

### For Team Distribution
```bash
# Create a ZIP file
cd target
zip -r OpenTeam-1.0.0-macOS.zip OpenTeam.app

# Or create a DMG (requires additional tools)
# hdiutil create -volname "OpenTeam" -srcfolder OpenTeam.app -ov -format UDZO OpenTeam-1.0.0.dmg
```

## 🔍 Troubleshooting

### App Won't Start
1. Check Console.app for error messages
2. Verify Java 21 is installed: `java -version`
3. Test the JAR directly: `java -jar OpenTeam.app/Contents/Java/OpenTeam.jar`

### Database Connection Issues  
1. Ensure PostgreSQL is running: `brew services list | grep postgresql`
2. Check config file exists: `ls -la ~/.openteam/config.yml`
3. Test database connection: `psql -U openteam_user -d openteam`

### Performance Issues
- Increase memory in the launcher script: `-Xmx2048m`
- Check system requirements (macOS 10.15+)

## 📝 Customization

### Change App Name
Edit `create-macos-app.sh` and modify:
```bash
APP_NAME="YourAppName"
BUNDLE_ID="com.yourcompany.yourapp"
```

### Add Custom Icon
Replace `src/main/resources/icons/openteam-logo.png` with your icon (1024x1024 PNG recommended).

### Modify JVM Options
Edit the launcher script in `OpenTeam.app/Contents/MacOS/OpenTeam` to add custom Java options.

## ✅ Verification Checklist

Before distributing:

- [ ] App launches without errors
- [ ] Database connection works
- [ ] All features function correctly
- [ ] Icon appears properly in dock
- [ ] App can be copied to Applications folder
- [ ] App works on a clean macOS system (test on another Mac)

---

**Note**: This packaging creates a macOS app bundle that requires Java 21 to be installed on the target system. For a completely self-contained app with embedded JRE, use the jpackage method (more complex setup required).