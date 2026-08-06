package natsystem.shared.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
public class Tool {
    public Integer parseIntSafe(String v) {
        if (v == null || v.isBlank()) return null;
        return Integer.parseInt(v);
    }

    public Double parseDoubleSafe(String v) {
        if (v == null || v.isBlank()) return 0.0;
        return Double.parseDouble(v);
    }

    public static void deleteTempFile(String fileName){
        try {
            Files.delete(Path.of(fileName));
        } catch (IOException e) {
            log.error("Erreur suppression fichier temporaire : " + e.getMessage());
        }
    }
}
