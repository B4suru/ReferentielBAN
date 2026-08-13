package natsystem.shared.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
public class Tool {
    public Integer parseIntSafe(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException _) {
            log.warn("Valeur entière invalide ignorée : " + v);
            return null;
        }
    }

    public Double parseDoubleSafe(String v) {
        if (v == null || v.isBlank()) return 0.0;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException _) {
            log.warn("Valeur décimale invalide ignorée : " + v);
            return 0.0;
        }
    }

    public static void deleteTempFile(String fileName){
        try {
            Files.delete(Path.of(fileName));
        } catch (IOException e) {
            log.error("Erreur suppression fichier temporaire : " + e.getMessage());
        }
    }
}
