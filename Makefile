# Thin wrapper over build.sh. Everything runs in Docker — no host JDK/Gradle.
# Usage: make build | make build-one V=8.15.5 | make test [V=7.13.1] | make docs
#        make versions [V=2.0.0] | make sync-vars | make clean

.PHONY: build build-one test docs versions sync-vars clean

build:
	./build.sh build

build-one:
	./build.sh build-one $(V)

test:
	./build.sh test $(V)

docs:
	./build.sh docs

versions:
	./build.sh versions $(V)

sync-vars:
	./build.sh sync-vars

clean:
	./build.sh clean
