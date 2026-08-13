package natsystem.tools;

import natsystem.shared.tools.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ToolTest {

    private final Tool tool = new Tool();

    // ---- parseIntSafe ----

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void parseIntSafe_shouldReturnNull_whenNullOrBlank(String input) {
        assertThat(tool.parseIntSafe(input)).isNull();
    }

    @Test
    void parseIntSafe_shouldReturnNull_whenNotNumeric() {
        assertThat(tool.parseIntSafe("ABC")).isNull();
        assertThat(tool.parseIntSafe("12.5")).isNull(); // pas un entier valide pour parseInt
    }

    @Test
    void parseIntSafe_shouldParseValidInteger() {
        assertThat(tool.parseIntSafe("75002")).isEqualTo(75002);
    }

    @Test
    void parseIntSafe_shouldTrimWhitespace() {
        assertThat(tool.parseIntSafe(" 75002 ")).isEqualTo(75002);
    }

    @Test
    void parseIntSafe_shouldHandleNegativeNumbers() {
        assertThat(tool.parseIntSafe("-5")).isEqualTo(-5);
    }

    // ---- parseDoubleSafe ----

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void parseDoubleSafe_shouldReturnZero_whenNullOrBlank(String input) {
        assertThat(tool.parseDoubleSafe(input)).isEqualTo(0.0);
    }

    @Test
    void parseDoubleSafe_shouldReturnNull_whenNotNumeric() {
        assertThat(tool.parseDoubleSafe("NOT_A_DOUBLE")).isEqualTo(0.0);
    }

    @Test
    void parseDoubleSafe_shouldParseValidDouble() {
        assertThat(tool.parseDoubleSafe("2.3315")).isEqualTo(2.3315);
    }

    @Test
    void parseDoubleSafe_shouldTrimWhitespace() {
        assertThat(tool.parseDoubleSafe(" 2.3315 ")).isEqualTo(2.3315);
    }


    // ---- deleteTempFile ----

    @Test
    void deleteTempFile_shouldDeleteExistingFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("a_supprimer.csv");
        Files.writeString(file, "contenu");
        assertThat(Files.exists(file)).isTrue();

        Tool.deleteTempFile(file.toString());

        assertThat(Files.exists(file)).isFalse();
    }
}
