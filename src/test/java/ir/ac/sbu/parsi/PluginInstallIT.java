package ir.ac.sbu.parsi;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Installs the locally built zip into a real Elasticsearch container and checks
 * the plugin loads and analyzes Persian text. Tagged {@code integration}: it
 * needs Docker, so it is excluded from the normal {@code test} task and run via
 * {@code gradle integrationTest} on a Docker-capable host/runner.
 */
@Tag("integration")
class PluginInstallIT {

    private static final String ES_VERSION = System.getProperty("parsi.esVersion", "7.13.1");
    private static final String DIST_DIR = System.getProperty("parsi.distDir", "build/distributions");
    private static final String BASE_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch";

    @Test
    void pluginInstallsAndAnalyzes() throws Exception {
        Path zip = findZip();

        ImageFromDockerfile image = new ImageFromDockerfile()
                .withFileFromPath("plugin.zip", zip)
                .withDockerfileFromBuilder(b -> b
                        .from(BASE_IMAGE + ":" + ES_VERSION)
                        .copy("plugin.zip", "/tmp/plugin.zip")
                        .run("bin/elasticsearch-plugin install --batch file:///tmp/plugin.zip")
                        .build());

        try (ElasticsearchContainer es = new ElasticsearchContainer(
                DockerImageName.parse(image.get()).asCompatibleSubstituteFor(BASE_IMAGE))
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node")) {
            es.start();

            String base = "http://" + es.getHttpHostAddress();
            HttpClient http = HttpClient.newHttpClient();

            HttpResponse<String> plugins = http.send(
                    HttpRequest.newBuilder(URI.create(base + "/_cat/plugins")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertTrue(plugins.body().contains("ParsiAnalyzer"),
                    "ParsiAnalyzer should be listed in _cat/plugins, got: " + plugins.body());

            HttpResponse<String> analyze = http.send(
                    HttpRequest.newBuilder(URI.create(base + "/_analyze"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(
                                    "{\"analyzer\":\"parsi\",\"text\":\"کتاب‌ها از روی میز\"}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, analyze.statusCode(), analyze.body());
            assertTrue(analyze.body().contains("\"tokens\""), analyze.body());
            assertTrue(analyze.body().contains("کتاب"),
                    "expected stem/lemma 'کتاب' in: " + analyze.body());
        }
    }

    private static Path findZip() throws IOException {
        Path dir = Paths.get(DIST_DIR);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
                "ParsiAnalyzer-*-elasticsearch-" + ES_VERSION + ".zip")) {
            for (Path p : stream) {
                return p;
            }
        }
        throw new IllegalStateException("No plugin zip for ES " + ES_VERSION + " in " + dir.toAbsolutePath()
                + " — build it first (./build.sh build-one " + ES_VERSION + ").");
    }
}
