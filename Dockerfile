# syntax=docker/dockerfile:1
#
# Host-clean build: nothing but Docker is needed on the host. The builder stage
# compiles the plugin for each requested Elasticsearch version; the final scratch
# stage holds just the zips so `docker build --output type=local,dest=./dist`
# drops them on the host with no container left behind.
ARG GRADLE_IMAGE=gradle:8.14-jdk21

FROM ${GRADLE_IMAGE} AS base
USER root
WORKDIR /work
COPY . .

# Build one zip per Elasticsearch version in ES_VERSIONS (space separated).
FROM base AS builder
ARG ES_VERSIONS
RUN set -eu; \
    : "${ES_VERSIONS:?set the ES_VERSIONS build-arg to a space-separated list}"; \
    mkdir -p /work/dist; \
    for v in $ES_VERSIONS; do \
        echo "==================== ParsiAnalyzer -> Elasticsearch $v ===================="; \
        gradle --no-daemon -PesVersion="$v" pluginZip; \
        cp build/distributions/ParsiAnalyzer-*-elasticsearch-"$v".zip /work/dist/; \
    done; \
    echo "=== artifacts ==="; ls -l /work/dist

# Unit tests (no container needed) for one representative version.
FROM base AS test
ARG TEST_ES_VERSION=7.13.1
RUN gradle --no-daemon -PesVersion="${TEST_ES_VERSION}" test

# Export-only stage: its filesystem is exactly the built zips.
FROM scratch AS export
COPY --from=builder /work/dist/ /
