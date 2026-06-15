# ParsiAnalyzer

ParsiAnalyzer is a Persian (Farsi) text-analysis plugin for Elasticsearch. It tokenizes,
normalizes, removes stop words, and reduces words to a search-friendly form (light stemming or
dictionary lemmatization) so that Persian search is high-precision and high-recall.

It is published as **one zip per Elasticsearch version** — Elasticsearch plugins are locked to an
exact version, so download the build that matches your cluster. See **[COMPATIBILITY.md](COMPATIBILITY.md)**.

> This is an independent continuation of the original ParsiAnalyzer by Nariman Esmailyfard.

## Analyzers

| Analyzer | Pipeline tail | Use it for |
|----------|---------------|------------|
| `parsi` | dictionary **lemmatization** + light-stem fallback | best recall on general Persian text |
| `parsi_light` | **light stemming** only | predictable, fast stemming |
| `parsi_standard` | **no** stemming/lemmatization | near-exact matching (normalization only) |

All three share: `zwnj_filter` (char) → `standard` tokenizer → lowercase → Persian normalization
(+ Arabic) → decimal-digit folding → stop-word removal → the tail above.

### Building-block filters

Compose your own analyzer from the registered components:

- char filter: `zwnj_filter` — converts spacing to ZWNJ (نیم‌فاصله) where Persian needs it
- token filters: `parsi_normalizer`, `parsi_stop_filter`, `parsi_stem_filter`, `parsi_lemmatizer`

Configurable settings:

- `parsi_stop_filter`: `stopwords` (array) to override the bundled list
- `parsi_stem_filter`: `min_stem_length` (int, default 3)

## Install

Download the zip for **your exact Elasticsearch version** from the
[Releases](https://github.com/salehi/ParsiAnalyzer/releases) page, then:

```
bin/elasticsearch-plugin install file:///path/to/ParsiAnalyzer-<version>-elasticsearch-<your-es-version>.zip
```

## Usage

```jsonc
POST _analyze
{ "analyzer": "parsi", "text": "روباه قهوه‌ای چابک از روی سگ تنبل می‌پرد" }
```

Map a field to it:

```jsonc
PUT /my_index
{ "mappings": { "properties": { "title": { "type": "text", "analyzer": "parsi" } } } }
```

## Build (host-clean, Docker only)

No JDK, Gradle, or Maven on your machine — everything runs in Docker, artifacts land in `dist/`.

```
make build              # build a zip for every version in compatibility.json -> dist/
make build-one V=8.15.5 # build a single version
make test               # run unit tests in the build image
make docs               # regenerate COMPATIBILITY.md from compatibility.json
make versions           # refresh compatibility.json from Maven Central, then docs
make sync-vars          # mirror compatibility.json into the ES_MATRIX repo variable
make clean              # remove dist/, build/, and the Docker build cache
```

(`./build.sh <same-subcommands>` works too, if you don't have `make`.)

### How versions are managed

`compatibility.json` is the **single source of truth**: it lists every supported Elasticsearch
version and the JDK it needs. It drives the build, the CI release matrix (via the `ES_MATRIX` repo
variable), and the generated `COMPATIBILITY.md`. Nothing is hand-typed — `make versions` regenerates
the list (latest patch of each 7.x/8.x/9.x minor) from Maven Central.

All Persian data (stop words, suffixes, normalization map, ZWNJ rules, lemmas) lives in data files
under `src/main/resources/ir/ac/sbu/parsi/data/` — never hard-coded in Java.

## Releasing

Push a tag (`git tag v2.0.0 && git push origin v2.0.0`). GitHub Actions builds the full matrix and
attaches every zip to the release. `workflow_dispatch` can build extra exact versions on demand.

## License

Apache-2.0 (see [LICENSE](LICENSE) and [NOTICE](NOTICE)). Bundled stop words are from
stopwords-iso/stopwords-fa (MIT).
