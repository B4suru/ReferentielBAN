package natsystem.shared.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;


@Slf4j
public class FileLocator {
    private FileLocator() {
    }

    public static List<Path> listFile (String folder) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(folder))) {
            return stream
                    .filter(Files::isRegularFile)
                    .toList();
        }
    }

    public static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static void downloadFile(String fileUrl, String folder, String fileName) {
        try (
                InputStream in = new URL(fileUrl).openStream();
                GZIPInputStream gzip = new GZIPInputStream(in)
        ) {
            Files.copy(gzip, Path.of(folder + "\\" + fileName));
        } catch (IOException e) {
            log.error("Erreur lors du téléchargement du fichier : " + e);
        }
    }
}
