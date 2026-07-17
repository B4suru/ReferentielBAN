package natSystem.BAN.tools;


import com.google.code.externalsorting.ExternalSort;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Outil de gestion de fichiers permettant l'écriture, la vérification,
 * le tri et le comptage de lignes de fichiers CSV.
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class FileManager {
	private String file;
	private FileWriter writer;

	public FileManager(String file) {
		this.file = file;
		try {
			this.writer = new FileWriter(file, true);
		} catch (IOException e) {
			System.err.println("Impossible d'ouvrir le fichier : " + file);
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
			System.err.println("Erreur lors du tri du CSV : " + e.getMessage());
		}
	}

	public long countFileLine() {
		long count = 0;
		Path path = Path.of(file);
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			while (reader.readLine() != null) {
				count++;
			}
		} catch (IOException e){
			System.err.println("Erreur lors du comptage du nombre de ligne du fichier");
		}
		return count;
	}

	public String getAbsolutePath() {
		Path path = Path.of(file);
		return path.toAbsolutePath().toString();
	}

	public boolean isFileEmpty () {
		return new File(file).length() <= 0;
	}

	public boolean isCsvValid(){

		String header =
				"id;id_fantoir;numero;rep;nom_voie;code_postal;code_insee;nom_commune;code_insee_ancienne_commune;nom_ancienne_commune;x;y;lon;lat;type_position;alias;nom_ld;libelle_acheminement;nom_afnor;source_position;source_nom_voie;certification_commune;cad_parcelles";
		if (!isFileEmpty()){
			try (BufferedReader reader = Files.newBufferedReader(Path.of(file))) {
				String premiereLigne = reader.readLine();
				if (premiereLigne != null && premiereLigne.equals(header)) {
					return true;
				}
			} catch (IOException e){
				System.err.println("Erreur de lecture de fichier lors de la vérification du csv : " + e);
			}
		}
		return false;
	}

	public void archiverFichier (String name){
		Path archiveDir = Path.of("Archive");
		try{
			Files.createDirectories(archiveDir);
		} catch (IOException e){
			System.err.println("Erreur lors de la création du répertoire archive : " + e);
		}


		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		String archiveFileName = "Archive/"+ LocalDateTime.now().format(formatter) + "_" + name;

		try {
			Files.move(Paths.get(file), Paths.get(archiveFileName));
		} catch (IOException e) {
			System.err.println("Erreur pendant l'archivage du ficher : " + e);
		}
	}
}
