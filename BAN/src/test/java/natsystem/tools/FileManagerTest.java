package natsystem.tools;

import lombok.extern.slf4j.Slf4j;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FileManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void getAbsolutePath_shouldReturnAbsolutePath() {
        Path file = tempDir.resolve("test.csv");
        FileManager fileManager = new FileManager(file.toString());

        String result = fileManager.getAbsolutePath();

        assertTrue(Path.of(result).isAbsolute());
        assertEquals(file.toAbsolutePath().toString(), result);

        fileManager.close();
    }

    @Test
    void isFileEmpty_shouldReturnTrue_whenFileIsEmpty() throws IOException {
        Path file = tempDir.resolve("empty.csv");
        Files.createFile(file);

        FileManager fileManager = new FileManager(file.toString());

        boolean result = fileManager.isFileEmpty();

        assertTrue(result);

        fileManager.close();
    }

    @Test
    void isFileEmpty_shouldReturnFalse_whenFileContainsData() throws IOException {
        Path file = tempDir.resolve("data.csv");
        Files.writeString(file, "id;nom;ville");

        FileManager fileManager = new FileManager(file.toString());
        boolean result = fileManager.isFileEmpty();

        assertFalse(result);

        fileManager.close();
    }


    @Test
    void isCsvValid_shouldReturnTrue_whenHeaderIsCorrect() throws IOException {
        Path file = tempDir.resolve("valid.csv");
        Files.writeString(
                file,
                """
                     id;nom;ville
                     1;Dupont;Paris
                     """
        );

        FileManager fileManager = new FileManager(file.toString());

        boolean result = fileManager.isCsvValid("id;nom;ville");

        assertTrue(result);

        fileManager.close();
    }

    @Test
    void isCsvValid_shouldReturnFalse_whenHeaderIsIncorrect() throws IOException {
        Path file = tempDir.resolve("invalid.csv");
        Files.writeString(
                file,
                """
                     id;nom;ville
                     1;Dupont;Paris
                     """
        );

        FileManager fileManager = new FileManager(file.toString());
        boolean result = fileManager.isCsvValid("id;nom;commune");

        assertFalse(result);

        fileManager.close();
    }

    @Test
    void isCsvValid_shouldReturnFalse_whenFileIsEmpty() throws IOException {
        Path file = tempDir.resolve("empty.csv");
        Files.createFile(file);

        FileManager fileManager = new FileManager(file.toString());
        boolean result = fileManager.isCsvValid("id;nom;ville");

        assertFalse(result);

        fileManager.close();
    }

    @Test
    void isCsvValid_shouldReturnFalse_whenFileDoesNotContainHeader() throws IOException {
        Path file = tempDir.resolve("data.csv");
        Files.writeString(
                file,
                """
                    1;Dupont;Paris
                    2;Martin;Lyon
                    """
        );

        FileManager fileManager = new FileManager(file.toString());
        boolean result = fileManager.isCsvValid("id;nom;ville");

        assertFalse(result);

        fileManager.close();
    }

    @Test
    void write_shouldAppendTextToFile() throws IOException {
        Path file = tempDir.resolve("write.csv");

        FileManager fileManager = new FileManager(file.toString());
        fileManager.write("ligne 1");
        fileManager.write("ligne 2");
        fileManager.close();

        List<String> lines = Files.readAllLines(file);

        assertEquals(2, lines.size());
        assertEquals("ligne 1", lines.get(0));
        assertEquals("ligne 2", lines.get(1));
    }

    @Test
    void open_shouldAppendToExistingFile() throws IOException {
        Path file = tempDir.resolve("append.csv");

        Files.writeString(file, "ligne existante\n");

        FileManager fileManager = new FileManager(file.toString());
        fileManager.write("nouvelle ligne");
        fileManager.close();

        List<String> lines = Files.readAllLines(file);

        assertEquals(2, lines.size());
        assertEquals("ligne existante", lines.get(0));
        assertEquals("nouvelle ligne", lines.get(1));
    }

    @Test
    void close_shouldCloseFileWriter() {
        Path file = tempDir.resolve("close.csv");

        FileManager fileManager = new FileManager(file.toString());

        fileManager.write("test");
        fileManager.close();

        assertNotNull(fileManager.getWriter());
    }

    @Test
    void archiverFichier_shouldMoveFileToArchive() throws IOException {
        Path file = tempDir.resolve("test.csv");

        Files.writeString(file, "id;nom\n1;Dupont\n");

        FileManager fileManager = new FileManager(file.toString());
        fileManager.close();
        fileManager.archiverFichier("test.csv");

        assertFalse(Files.exists(file));

        Path archiveDir = Path.of("Archive");

        assertTrue(Files.exists(archiveDir));

        try (var files = Files.list(archiveDir)) {
            assertTrue(
                    files.anyMatch(path ->
                            path.getFileName()
                                    .toString()
                                    .endsWith("_test.csv")
                    )
            );
        }

        // Nettoyage de l'archive créée
        try (var files = Files.list(archiveDir)) {
            files.filter(path ->
                            path.getFileName()
                                    .toString()
                                    .endsWith("_test.csv")
                    )
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("Erreur lors de la suppression du fichier de test : {}", e.getMessage());
                        }
                    });
        }
    }

    @Test
    void sortCSV_shouldSortFileAndKeepHeaderFirst() throws IOException {
        Path file = tempDir.resolve("test.csv");

        Files.writeString(
                file,
                """
                   id;nom;ville
                   3;Martin;Lyon
                   1;Dupont;Paris
                   2;Durand;Nice
                   """
        );

        FileManager fileManager = new FileManager(file.toString());
        fileManager.close();

        fileManager.sortCSV();

        Path sortedFile = Path.of("csv_sorted.csv");

        assertTrue(Files.exists(sortedFile));

        List<String> lines = Files.readAllLines(sortedFile);

        assertEquals("id;nom;ville", lines.get(0));
        assertEquals("1;Dupont;Paris", lines.get(1));
        assertEquals("2;Durand;Nice", lines.get(2));
        assertEquals("3;Martin;Lyon", lines.get(3));
        assertEquals(4, lines.size());

        Files.deleteIfExists(sortedFile);
    }
}
