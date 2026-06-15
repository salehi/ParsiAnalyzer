#!/usr/bin/env bash
#
# Generate COMPATIBILITY.md from compatibility.json. Never edit COMPATIBILITY.md by hand.
# Usage: scripts/gen-compat-doc.sh
set -euo pipefail
cd "$(dirname "$0")/.."

ver="$(jq -r '.version' compatibility.json)"
count="$(jq '.targets | length' compatibility.json)"

{
  echo "# Compatibility Matrix"
  echo
  echo "ParsiAnalyzer **$ver** is published as one zip per Elasticsearch version. Pick the build that"
  echo "matches the exact version in your cluster (Elasticsearch plugins are version-locked)."
  echo
  echo "> Generated from [\`compatibility.json\`](compatibility.json) — do not edit by hand."
  echo "> Run \`make docs\` (or \`./build.sh docs\`) to regenerate. $count targets."
  echo
  echo "| ParsiAnalyzer | Elasticsearch | Java |"
  echo "|---|---|---|"
  jq -r --arg v "$ver" '.targets[] | "| \($v) | \(.elasticsearch) | \(.java) |"' compatibility.json
} > COMPATIBILITY.md

echo "Wrote COMPATIBILITY.md ($count rows)."
