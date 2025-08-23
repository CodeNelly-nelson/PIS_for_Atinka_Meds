#!/bin/bash
# Run Atinka Meds on macOS

# Resolve to this script's directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Ensure data dir exists next to the script (not inside jar)
mkdir -p data

echo "Starting Atinka Meds..."
echo

# Require Java 17+
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java runtime not found. Please install Java 17+."
  read -r -p "Press Enter to close..."
  exit 1
fi

# Run the jar from ./dist
java -jar "dist/AtinkaMeds.jar"

echo
echo "Program finished. Press Enter to close..."
read -r
