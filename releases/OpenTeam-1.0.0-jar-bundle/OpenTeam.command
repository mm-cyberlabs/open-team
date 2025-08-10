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
