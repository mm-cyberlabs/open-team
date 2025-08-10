#!/usr/bin/env bash
set -euo pipefail
#!/usr/bin/env bash
set -euo pipefail

APP_NAME="OpenTeam"

# Resolve app version from Maven
APP_VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"
MAIN_JAR="open-team-app-${APP_VERSION}-shaded.jar"

echo "Building ${APP_NAME} JAR bundle (version: ${APP_VERSION})..."

# 1) Build the shaded JAR
mvn clean package -DskipTests

if [ ! -f "target/${MAIN_JAR}" ]; then
  echo "Error: shaded JAR not found at target/${MAIN_JAR}"
  exit 1
fi

# 2) Prepare bundle folder
BUNDLE_DIR="releases/${APP_NAME}-${APP_VERSION}-jar-bundle"
rm -rf "${BUNDLE_DIR}"
mkdir -p "${BUNDLE_DIR}"

# 3) Copy the application JAR
cp "target/${MAIN_JAR}" "${BUNDLE_DIR}/${APP_NAME}.jar"

# 4) Create a macOS-friendly launcher (fallback if double-clicking the JAR doesn’t work)
cat > "${BUNDLE_DIR}/${APP_NAME}.command" << 'EOF'
#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="${APP_DIR}/OpenTeam.jar"
OPENTEAM_DIR="${HOME}/.openteam"
CONFIG_FILE="${OPENTEAM_DIR}/config.yml"
LOGS_DIR="${OPENTEAM_DIR}/logs"
mkdir -p "${LOGS_DIR}"
LOG_FILE="${LOGS_DIR}/openteam-$(date +%Y%m%d-%H%M%S).log"

log() { echo "$(date '+%Y-%m-%d %H:%M:%S') $*" | tee -a "${LOG_FILE}"; }

log "Launcher started. Using log file: ${LOG_FILE}"

# 1) Check Java presence
if ! command -v java >/dev/null 2>&1; then
  if [ "${OPENTEAM_CLI_MODE:-}" = "1" ]; then
    log "Java is required. Visit: https://adoptium.net/temurin/releases/?version=21"
    exit 1
  fi
  log "Java not found. Prompting user to install."
  osascript -e 'display dialog "Java is required to run OpenTeam. Click OK to open the Java download page." buttons {"OK"} default button "OK" with icon caution' || true
  open "https://adoptium.net/temurin/releases/?version=21" || true
  exit 1
fi

# 2) Ensure ~/.openteam and config.yml
mkdir -p "${OPENTEAM_DIR}"

if [ ! -f "${CONFIG_FILE}" ]; then
  log "Config file not found. Creating starter config at ${CONFIG_FILE}"
  cat > "${CONFIG_FILE}" << 'YAML'
# OpenTeam configuration
# Fill in your values, save, and relaunch.
database:
  url: "jdbc:postgresql://localhost:5432/openteam"
  username: "openteam_user"
  password: "your_secure_password"
  driver: "org.postgresql.Driver"

application:
  refreshInterval: 10
YAML

  if [ "${OPENTEAM_CLI_MODE:-}" = "1" ]; then
    log "Starter config created. Exiting due to CLI mode."
    exit 0
  fi

  log "Starter config created. Opening for editing and exiting."
  osascript -e 'display dialog "A starter config has been created at ~/.openteam/config.yml. Please edit it now, save, and relaunch." buttons {"OK"} default button "OK" with icon note' || true
  open -e "${CONFIG_FILE}" || true
  exit 0
fi

# 3) Launch the app
if [ "${OPENTEAM_CLI_MODE:-}" = "1" ]; then
  log "CLI mode enabled. Skipping actual launch."
  exit 0
fi

log "Launching application JAR: ${JAR_PATH}"
exec java -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -jar "${JAR_PATH}" >> "${LOG_FILE}" 2>&1
EOF

chmod +x "${BUNDLE_DIR}/${APP_NAME}.command"

# 5) Optional: zip the bundle for distribution
(
  cd "releases"
  zip -r "${APP_NAME}-${APP_VERSION}-jar-bundle.zip" "${APP_NAME}-${APP_VERSION}-jar-bundle" >/dev/null
)

echo "Done!"
echo "Bundle folder: ${BUNDLE_DIR}"
echo "Distributable zip: releases/${APP_NAME}-${APP_VERSION}-jar-bundle.zip"

echo
echo "Usage:"
echo "- Double-click ${APP_NAME}.jar (works if macOS associates .jar with Java)."
echo "- If not, double-click ${APP_NAME}.command (verifies Java, ensures ~/.openteam/config.yml, and launches)."
echo
echo "Note (macOS quarantine): If you downloaded this zip from the internet, you may need:"
echo "  xattr -dr com.apple.quarantine \"${BUNDLE_DIR}\""
APP_NAME="OpenTeam"

# Resolve app version from Maven
APP_VERSION="$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)"
MAIN_JAR="open-team-app-${APP_VERSION}-shaded.jar"

echo "Building ${APP_NAME} JAR bundle (version: ${APP_VERSION})..."

# 1) Build the shaded JAR
mvn clean package -DskipTests

if [ ! -f "target/${MAIN_JAR}" ]; then
  echo "Error: shaded JAR not found at target/${MAIN_JAR}"
  exit 1
fi

# 2) Prepare bundle folder
BUNDLE_DIR="releases/${APP_NAME}-${APP_VERSION}-jar-bundle"
rm -rf "${BUNDLE_DIR}"
mkdir -p "${BUNDLE_DIR}"

# 3) Copy the application JAR
cp "target/${MAIN_JAR}" "${BUNDLE_DIR}/${APP_NAME}.jar"

# 4) Create a macOS-friendly launcher (fallback if double-clicking the JAR doesn’t work)
cat > "${BUNDLE_DIR}/${APP_NAME}.command" << 'EOF'
#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="${APP_DIR}/OpenTeam.jar"
OPENTEAM_DIR="${HOME}/.openteam"
CONFIG_FILE="${OPENTEAM_DIR}/config.yml"
LOGS_DIR="${OPENTEAM_DIR}/logs"
mkdir -p "${LOGS_DIR}"
LOG_FILE="${LOGS_DIR}/openteam-$(date +%Y%m%d-%H%M%S).log"

log() { echo "$(date '+%Y-%m-%d %H:%M:%S') $*" | tee -a "${LOG_FILE}"; }

log "Launcher started. Using log file: ${LOG_FILE}"

# 1) Check Java presence
if ! command -v java >/dev/null 2>&1; then
  log "Java not found. Prompting user to install."
  osascript -e 'display dialog "Java is required to run OpenTeam. Click OK to open the Java download page." buttons {"OK"} default button "OK" with icon caution' || true
  open "https://adoptium.net/temurin/releases/?version=21" || true
  exit 1
fi

# 2) Ensure ~/.openteam and config.yml
mkdir -p "${OPENTEAM_DIR}"

if [ ! -f "${CONFIG_FILE}" ]; then
  log "Config file not found. Creating starter config at ${CONFIG_FILE}"
  cat > "${CONFIG_FILE}" << 'YAML'
# OpenTeam configuration
# Fill in your values, save, and relaunch.
database:
  url: "jdbc:postgresql://localhost:5432/openteam"
  username: "openteam_user"
  password: "your_secure_password"
  driver: "org.postgresql.Driver"

application:
  refreshInterval: 10
YAML

  log "Starter config created. Opening for editing and exiting."
  osascript -e 'display dialog "A starter config has been created at ~/.openteam/config.yml. Please edit it now, save, and relaunch." buttons {"OK"} default button "OK" with icon note' || true
  open -e "${CONFIG_FILE}" || true
  exit 0
fi

# 3) Launch the app
log "Launching application JAR: ${JAR_PATH}"
exec java -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -jar "${JAR_PATH}" >> "${LOG_FILE}" 2>&1
EOF

chmod +x "${BUNDLE_DIR}/${APP_NAME}.command"

# 5) Optional: zip the bundle for distribution
(
  cd "releases"
  zip -r "${APP_NAME}-${APP_VERSION}-jar-bundle.zip" "${APP_NAME}-${APP_VERSION}-jar-bundle" >/dev/null
)

echo "Done!"
echo "Bundle folder: ${BUNDLE_DIR}"
echo "Distributable zip: releases/${APP_NAME}-${APP_VERSION}-jar-bundle.zip"

echo
echo "Usage:"
echo "- Double-click ${APP_NAME}.jar (works if macOS associates .jar with Java)."
echo "- If not, double-click ${APP_NAME}.command (verifies Java, ensures ~/.openteam/config.yml, and launches)."
echo
echo "Note (macOS quarantine): If you downloaded this zip from the internet, you may need:"
echo "  xattr -dr com.apple.quarantine \"${BUNDLE_DIR}\""
