package natSystem.BAN.tools;


import com.google.code.externalsorting.ExternalSort;
import lombok.AllArgsConstructor;
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
import java.util.Comparator;
import java.util.List;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class FileManager {
	private String fileName;
	private FileWriter writer;

	public FileManager(String fileName) {
		this.fileName = fileName;
		try {
			this.writer = new FileWriter(fileName, true);
		} catch (IOException e) {
			System.err.println("Impossible d'ouvrir le fichier : " + fileName);
		}
	}

	public void write(String text) {
		try {
			writer.write(text + '\n');
			writer.flush();
		} catch (IOException e) {
			System.err.println("Erreur écriture : " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (writer != null) writer.close();
		} catch (IOException e) {
			System.err.println("Erreur fermeture : " + e.getMessage());
		}
	}
	
	public boolean isCSVExisting() {
		Path chemin = Path.of(fileName);
		if (!Files.isRegularFile(chemin)
		        || !chemin.getFileName().toString().toLowerCase().endsWith(".csv")) {
		    System.out.println("Ce n'est pas un fichier CSV.");
		}
		return Files.isRegularFile(chemin) && chemin.getFileName().toString().toLowerCase().endsWith(".csv");
	}

	public void sortCSV() {
		File input = new File(fileName);
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
			System.err.println("Erreur lors du tri du CSV : " + e.getMessage());
		}
	}

	public long countFileLine() {
		long count = 0;
		Path path = Path.of(fileName);
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			while (reader.readLine() != null) {
				count++;
			}
			log.info("Nombre de ligne : " + count);
		} catch (IOException e){
			System.err.println("Erreur lors du comptage du nombre de ligne du fichier");
		}

		return count;
	}
}
