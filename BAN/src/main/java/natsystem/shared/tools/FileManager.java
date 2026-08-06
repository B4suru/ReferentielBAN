package natsystem.shared.tools;


import com.google.code.externalsorting.ExternalSort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Outil de gestion de fichiers permettant l'écriture, la vérification,
 * le tri et le comptage de lignes de fichiers.
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class FileManager {
	private String file;
	private FileWriter writer;

	public FileManager(String file) {
		open(file);
	}

	public synchronized void open(String file) {
		this.file = file;
		try {
			this.writer = new FileWriter(file, true);
		} catch (IOException e) {
			log.error("Impossible d'ouvrir le fichier : " + file + "(" + e + ")");
		}
	}

	public synchronized void write(String text) {
		try {
			writer.write(text + '\n');
			writer.flush();
		} catch (IOException e) {
			log.error("Erreur écriture : " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (writer != null) writer.close();
		} catch (IOException e) {
			log.error("Erreur fermeture : " + e.getMessage());
		}
	}

	public void sortCSV() {
		File input = new File(file);
		File output = new File("csv_sorted.csv");

		Comparator<String> comparator = (a, b) -> {
			if (a.startsWith("id;")) return -1;
			if (b.startsWith("id;")) return 1;
			return a.compareTo(b);
		};

		try {
			List<File> tmp = ExternalSort.sortInBatch(input, comparator);
			ExternalSort.mergeSortedFiles(tmp, output, comparator);
		} catch (IOException e) {
			log.error("Erreur lors du tri du CSV : " + e.getMessage());
		}
	}

	public String getAbsolutePath() {
		Path path = Path.of(file);
		return path.toAbsolutePath().toString();
	}

	public boolean isFileEmpty () {
		return new File(file).length() <= 0;
	}

	public boolean isCsvValid(String header){

		if (!isFileEmpty()){
			try (BufferedReader reader = Files.newBufferedReader(Path.of(file))) {
				String premiereLigne = reader.readLine();
				if (premiereLigne != null && premiereLigne.equals(header)) {
					return true;
				}
			} catch (IOException e){
				log.error("Erreur de lecture de fichier lors de la vérification du csv : " + e);
			}
		}
		return false;
	}

	public void archiverFichier (String name){
		Path archiveDir = Path.of("Archive");
		try{
			Files.createDirectories(archiveDir);
		} catch (IOException e){
			log.error("Erreur lors de la création du répertoire archive : " + e);
		}


		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		String archiveFileName = "Archive/"+ LocalDateTime.now(ZoneId.systemDefault()).format(formatter) + "_" + name;

		try {
			Files.move(Paths.get(file), Paths.get(archiveFileName));
		} catch (IOException e) {
			log.error("Erreur pendant l'archivage du ficher : " + e);
		}
	}
}
