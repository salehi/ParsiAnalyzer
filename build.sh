#!/usr/bin/env bash
#
# Host-clean build driver. Everything runs inside Docker; the only host
# requirements are Docker (with BuildKit) and — for docs/versions/sync-vars —
# jq, curl and gh. Run `./build.sh` (or `make`) — never install a JDK/Gradle.
set -euo pipefail
cd "$(dirname "$0")"

DIST=dist

versions() { jq -r '.targets[].elasticsearch' compatibility.json | tr '\n' ' '; }

do_build() {
    local vers="$1"
    echo "Building ParsiAnalyzer for Elasticsearch: ${vers}"
    docker build --target export --build-arg ES_VERSIONS="${vers}" \
        --output "type=local,dest=./${DIST}" .
    echo "=== ./${DIST} ==="
    ls -1 "${DIST}"
}

case "${1:-build}" in
    build)
        do_build "$(versions)"
        ;;
    build-one)
        [ "$#" -ge 2 ] || { echo "usage: ./build.sh build-one <esVersion>" >&2; exit 1; }
        do_build "$2"
        ;;
    test)
        docker build --target test --build-arg TEST_ES_VERSION="${2:-7.13.1}" . >/dev/null \
            && echo "unit tests passed (ES ${2:-7.13.1})"
        ;;
    docs)
        ./scripts/gen-compat-doc.sh
        ;;
    versions)
        ./scripts/update-compatibility.sh "${2:-}" && ./scripts/gen-compat-doc.sh
        ;;
    sync-vars)
        gh variable set ES_MATRIX < compatibility.json
        echo "ES_MATRIX repo variable updated from compatibility.json"
        ;;
    clean)
        rm -rf "${DIST}" build .gradle
        docker builder prune -f >/dev/null 2>&1 || true
        echo "removed ${DIST}/, build/, .gradle/ and pruned the Docker build cache"
        ;;
    *)
        echo "usage: ./build.sh {build | build-one <esVersion> | test [esVersion] | docs | versions [pluginVersion] | sync-vars | clean}" >&2
        exit 1
        ;;
esac
