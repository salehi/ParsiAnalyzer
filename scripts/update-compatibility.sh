#!/usr/bin/env bash
#
# Regenerate compatibility.json — the single source of truth for the build matrix,
# the ES_MATRIX repo variable, and COMPATIBILITY.md.
#
# Picks the LATEST PATCH of every minor across Elasticsearch 7.x / 8.x / 9.x and
# assigns the JDK each line needs. No version is ever hand-typed.
#
# Usage: scripts/update-compatibility.sh [plugin-version]
set -euo pipefail
cd "$(dirname "$0")/.."

PLUGIN_VERSION="${1:-$(jq -r '.version // "2.0.0"' compatibility.json 2>/dev/null || echo 2.0.0)}"
META_URL="https://repo1.maven.org/maven2/org/elasticsearch/elasticsearch/maven-metadata.xml"

tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT
curl -fsSL "$META_URL" -o "$tmp"

# GA X.Y.Z versions (excludes -alpha/-beta/-rc), latest patch per minor, ascending.
latest_per_minor="$(
  grep -oE '<version>[789]\.[0-9]+\.[0-9]+</version>' "$tmp" \
    | sed -E 's#</?version>##g' \
    | sort -V \
    | awk -F. '{ v[$1"."$2] = $0 } END { for (k in v) print v[k] }' \
    | sort -V
)"

# Map each ES version to the JDK that line builds/runs on.
#   7.0-7.5 -> 8, 7.6-7.17 -> 11, 8.x/9.x -> 17
matrix_tsv="$(
  while read -r ver; do
    [ -z "$ver" ] && continue
    major="${ver%%.*}"
    minor="$(printf '%s' "$ver" | cut -d. -f2)"
    if   [ "$major" = "7" ] && [ "$minor" -le 5 ]; then java=8
    elif [ "$major" = "7" ];                        then java=11
    else                                                 java=17
    fi
    printf '%s\t%s\n' "$ver" "$java"
  done <<< "$latest_per_minor"
)"

jq -Rn --arg version "$PLUGIN_VERSION" '
  [ inputs | split("\t") | { elasticsearch: .[0], java: .[1] } ]
  | { version: $version, targets: . }
' <<< "$matrix_tsv" > compatibility.json

echo "Wrote compatibility.json: $(jq '.targets | length' compatibility.json) targets (plugin $PLUGIN_VERSION)."
