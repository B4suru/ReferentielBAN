package natSystem.BAN.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Outil fournissant des méthodes de conversion sécurisée de chaînes de
 * caractères vers des types numériques.
 */
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
            System.err.println("Erreur suppression fichier temporaire : " + e.getMessage());
        }
    }
}
