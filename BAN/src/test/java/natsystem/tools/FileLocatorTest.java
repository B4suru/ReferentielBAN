package natsystem.tools;

import com.sun.net.httpserver.HttpServer;
import natsystem.shared.tools.FileLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class FileLocatorTest {

    @Test
    void listFile_shouldReturnOnlyRegularFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("a.csv"), "contenu a");
        Files.writeString(tempDir.resolve("b.csv"), "contenu b");
        Files.createDirectory(tempDir.resolve("sous_dossier"));

        List<Path> files = FileLocator.listFile(tempDir.toString());
        List<String> fileNames = files.stream().map(p -> p.getFileName().toString()).toList();

        assertThat(fileNames.get(0)).isEqualTo("a.csv");
        assertThat(fileNames.get(1)).isEqualTo("b.csv");
    }

    @Test
    void listFile_shouldReturnEmptyList_whenFolderIsEmpty(@TempDir Path tempDir) throws IOException {
        assertThat(FileLocator.listFile(tempDir.toString())).isEqualTo(List.of());
    }

    // ---- sha256 ----

    @Test
    void sha256_shouldMatchKnownValue_forFixedContent(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("data.csv");
        Files.writeString(file, "id;nom\n1;Test\n", StandardCharsets.UTF_8);

        String hash = FileLocator.sha256(file);

        assertThat(hash).isEqualTo("8d7c3d69713bbf873ac0e223ee016e84242ce8563b16c538112919aca0945c7f");
    }

    @Test
    void sha256_shouldBeLowercaseHexOf64Characters(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("data.csv");
        Files.writeString(file, "peu importe le contenu");

        assertThat(FileLocator.sha256(file)).matches("^[0-9a-f]{64}$");
    }

    @Test
    void sha256_shouldBeDeterministic_forIdenticalContent(@TempDir Path tempDir) throws Exception {
        Path file1 = tempDir.resolve("data1.csv");
        Path file2 = tempDir.resolve("data2.csv");
        Files.writeString(file1, "même contenu");
        Files.writeString(file2, "même contenu");

        assertThat(FileLocator.sha256(file1)).isEqualTo(FileLocator.sha256(file2));
    }

    @Test
    void sha256_shouldDiffer_whenContentDiffers(@TempDir Path tempDir) throws Exception {
        Path file1 = tempDir.resolve("data1.csv");
        Path file2 = tempDir.resolve("data2.csv");
        Files.writeString(file1, "contenu A");
        Files.writeString(file2, "contenu B");

        assertThat(FileLocator.sha256(file1)).isNotEqualTo(FileLocator.sha256(file2));
    }

    @Test
    void downloadFile_shouldDownloadAndDecompressGzipContent(@TempDir Path tempDir) throws Exception {
        String expectedContent = "id;nom\n1;Test\n";
        byte[] gzipped = gzip(expectedContent.getBytes(StandardCharsets.UTF_8));

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/data.csv.gz", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/gzip");
            exchange.sendResponseHeaders(200, gzipped.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(gzipped);
            }
        });
        server.start();

        Path outputPath = Path.of(tempDir + "\\output.csv");
        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/data.csv.gz";

            FileLocator.downloadFile(url, tempDir.toString(), "output.csv");

            assertThat(Files.exists(outputPath)).isTrue();
            assertThat(Files.readString(outputPath, StandardCharsets.UTF_8)).isEqualTo(expectedContent);
        } finally {
            server.stop(0);
            Files.deleteIfExists(outputPath);
        }
    }

    @Test
    void downloadFile_shouldNotThrow_whenHostIsUnreachable(@TempDir Path tempDir) {
        String unreachableUrl = "http://localhost:1/fichier.gz";

        assertThatCode(() -> FileLocator.downloadFile(unreachableUrl, tempDir.toString(), "output.csv"))
                .doesNotThrowAnyException();

        assertThat(Files.exists(Path.of(tempDir + "\\output.csv"))).isFalse();
    }

    @Test
    void downloadFile_shouldNotThrow_whenUrlIsMalformed(@TempDir Path tempDir) {
        assertThatCode(() -> FileLocator.downloadFile("ceci n'est pas une url", tempDir.toString(), "output.csv"))
                .doesNotThrowAnyException();
    }

    private byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(data);
        }
        return baos.toByteArray();
    }
}
