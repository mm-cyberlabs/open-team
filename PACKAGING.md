# 📦 OpenTeam Application Packaging Guide

This guide explains how to package the OpenTeam JavaFX application for macOS distribution.

## 🚀 Quick Start - Create Portable macOS App

The easiest way to create a **truly portable** macOS application with embedded JRE:

```bash
# 1. Make sure you're in the project root directory
cd /path/to/open-team

# 2. Run the portable packaging script
./create-portable-macos-app.sh
```

This will create `target/OpenTeam.app` that:
- **Works on ANY macOS machine** (no Java installation required)
- Contains embedded Java 21 runtime
- Is completely self-contained
- Only needs configuration file: `~/.openteam/config.yml`

## 📋 Prerequisites (Developer Only)

**For building the portable app:**
- **Java 21**: Make sure you have Java 21 installed (for building only)
- **Maven**: For building the project
- **macOS**: This guide is specific to macOS packaging

**For end users:** NO prerequisites! The portable app includes everything needed.

## 🛠️ Packaging Methods

### Method 1: Portable App with Embedded JRE (RECOMMENDED)

Create a fully self-contained app:

```bash
# Build portable app with embedded Java runtime
./create-portable-macos-app.sh

# Alternative: Using Maven profile
mvn clean package -Pportable-macos
```

**Benefits:**
- ✅ No Java installation required on target machines
- ✅ Works on any macOS 10.15+ system
- ✅ True "double-click to run" experience
- ✅ Professional distribution ready

### Method 2: Legacy App Bundle (Requires Java on target)

Create traditional app bundle (requires Java 21 on target machine):

```bash
./create-macos-app.sh
```

**Note:** This method is deprecated as it requires users to install Java 21.

### Method 3: Simple JAR Distribution

For developers who want to run from command line:

```bash
# Build and run
mvn clean package -DskipTests
java -jar target/open-team-app-1.0.0-shaded.jar
```

## 📁 App Bundle Structure

The portable app bundle has this structure:

```
OpenTeam.app/
├── Contents/
│   ├── Info.plist          # App metadata
│   ├── PkgInfo             # Package type info
│   ├── MacOS/
│   │   └── OpenTeam        # Native launcher
│   ├── Resources/
│   │   └── OpenTeam.icns   # App icon
│   ├── runtime/            # ← Embedded Java 21 JRE (NEW!)
│   │   └── Contents/
│   │       └── Home/
│   └── app/
│       └── open-team-app-1.0.0-shaded.jar
```

## 🔧 Configuration Requirements

**End users only need:**

### 1. Configuration File

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

### 2. Database Access

- PostgreSQL server running and accessible
- Network connectivity to the database

**That's it!** No Java installation, no Maven, no development tools required.

## 🚀 Distribution

### For Personal Use
```bash
# Copy to Applications
cp -r target/OpenTeam.app /Applications/
```

### For Team Distribution
```bash
# Create a ZIP file for easy sharing
cd target
zip -r OpenTeam-1.0.0-macOS-portable.zip OpenTeam.app

# Users just need to:
# 1. Download and unzip
# 2. Copy OpenTeam.app to Applications
# 3. Create ~/.openteam/config.yml
# 4. Run the app
```

## 🔍 Troubleshooting

### App Won't Start
1. Check Console.app for error messages
2. Verify the config file exists: `ls -la ~/.openteam/config.yml`
3. Test database connection from terminal

### Database Connection Issues  
1. Ensure PostgreSQL is running: `brew services list | grep postgresql`
2. Check config file format (YAML syntax)
3. Test database connection: `psql -U openteam_user -d openteam`

### Performance Issues
- The app includes optimized JVM settings for 1GB memory allocation
- Minimum system requirements: macOS 10.15+ (Catalina or later)

## 📝 Customization

### Change App Name
Edit `create-portable-macos-app.sh` and modify:
```bash
APP_NAME="YourAppName"
BUNDLE_ID="com.yourcompany.yourapp"
```

### Add Custom Icon
Replace `src/main/resources/icons/openteam-logo.png` with your icon (1024x1024 PNG recommended).

### Modify JVM Options
Edit the script and adjust:
```bash
--java-options "-Xmx2048m"  # Increase memory
--java-options "-Dmy.custom.property=value"  # Add custom properties
```

## ✅ Distribution Checklist

Before distributing:

- [ ] App launches without errors on your machine
- [ ] App launches on a different Mac (test portability)
- [ ] Database connection works with config file
- [ ] All features function correctly
- [ ] Icon appears properly in dock and Finder
- [ ] App can be copied to Applications folder
- [ ] ZIP file extracts correctly

## 🎯 Migration from Legacy Method

If you were using the old `create-macos-app.sh`:

1. **Switch to new script:** Use `./create-portable-macos-app.sh`
2. **Update documentation:** Tell users they don't need Java anymore
3. **Test thoroughly:** Verify the embedded JRE works on target machines
4. **Redistribute:** Create new ZIP files with the portable app

---

**Key Advantage:** The portable method creates apps that work on **any macOS machine** with just the configuration file. No more "install Java first" instructions!