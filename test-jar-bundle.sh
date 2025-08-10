#!/usr/bin/env bash
set -euo pipefail

echo "=== Testing JAR bundle build and launchers ==="

# Ensure scripts are executable
chmod +x ./create-jar-bundle.sh || true
chmod +x ./run-app.sh || true

# Operate in CLI test mode (no GUI prompts/launch)
export OPENTEAM_CLI_MODE=1

# Use a temporary HOME to avoid touching the real ~/.openteam
ORIG_HOME="${HOME}"
TEST_HOME="$(mktemp -d)"
trap 'EXIT_CODE=$?; rm -rf "${TEST_HOME}"; export HOME="${ORIG_HOME}"; exit ${EXIT_CODE}' EXIT
export HOME="${TEST_HOME}"

echo "[1/5] Building JAR bundle..."
./create-jar-bundle.sh

echo "[2/5] Locating bundle directory..."
BUNDLE_DIR="$(ls -d releases/OpenTeam-*-jar-bundle 2>/dev/null | head -n1 || true)"
if [ -z "${BUNDLE_DIR}" ] || [ ! -d "${BUNDLE_DIR}" ]; then
  echo "FAIL: Bundle directory not found under releases/"
  exit 1
fi
echo "Bundle: ${BUNDLE_DIR}"

echo "[3/5] Verifying bundle artifacts..."
test -f "${BUNDLE_DIR}/OpenTeam.jar" || { echo "FAIL: OpenTeam.jar missing"; exit 1; }
test -f "${BUNDLE_DIR}/OpenTeam.command" || { echo "FAIL: OpenTeam.command missing"; exit 1; }

echo "[4/5] Testing config creation via macOS launcher (CLI mode)..."
bash "${BUNDLE_DIR}/OpenTeam.command"
test -f "${HOME}/.openteam/config.yml" || { echo "FAIL: ~/.openteam/config.yml not created by launcher"; exit 1; }

echo "[5/5] Testing run-app.sh prelaunch checks (CLI mode, skip GUI)..."
./run-app.sh

echo "All tests passed."
