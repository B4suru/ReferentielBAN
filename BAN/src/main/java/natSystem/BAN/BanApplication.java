package natSystem.BAN;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.tools.ScannerTool;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import natSystem.BAN.tools.FileManager;

@Slf4j
@OpenAPIDefinition(
		info = @Info(
				title = "Ban api",
				version = "1.0",
				description = "API documentation pour référentiel BAN"
		)
)
@SpringBootApplication
public class BanApplication {
	public static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run(JobOperator launcher, Job banBatchJob) {
		return args -> {
			FileManager logs = new FileManager("Logs.txt");
			logs.write("============================ Debuts logs ============================");
			ScannerTool scanner = new ScannerTool();

			boolean isNotFinished = true;
			do {
				//Recuperation du fichier csv
				FileManager csvFile = new FileManager();
				do {
					String cheminFichierCsv = scanner.getString("Chemin du fichier csv : ");
					csvFile.setFileName(cheminFichierCsv);
				} while (!csvFile.isCSVExisting() || csvFile.getFileName().isEmpty());

				//Récuperation des filtres
				Long codePostalInput = scanner.getLong("Code postal (laisser vide pour ignorer) : ");
				Long codeInseeInput = scanner.getLong("Code INSEE (laisser vide pour ignorer) : ");

				//Début timer traitement
				LocalDateTime start = LocalDateTime.now();
				logs.write("Date : " + start);
				logs.write("Fichier : " + csvFile.getFileName());
				logs.write("Filtre : [Code postal : " + codePostalInput + "] | [Code insee: " + codeInseeInput+ "]");


				//Récuperation de la taille du csv
				long sizeCSV = csvFile.countFileLine();

				//Tri du fichier csv
				LocalDateTime startSort = LocalDateTime.now();
				csvFile.sortCSV();
				LocalDateTime endSort = LocalDateTime.now();
				Duration dureeSort = Duration.between(startSort, endSort);
				long minutesSort = dureeSort.toMinutes();
				long secondesSort = dureeSort.minusMinutes(minutesSort).getSeconds();
				log.info("Temps tri csv : " + minutesSort + " min "+ secondesSort+" sec");
				logs.write(String.format("Temps tri csv : %d min %d sec", minutesSort, secondesSort));

				//Creation des paramètres et lancement du job
				JobParametersBuilder builder = new JobParametersBuilder()
						.addLong("startAt", System.currentTimeMillis())
						.addLong("sizeCSV", sizeCSV);
				if (codePostalInput != null) {
					builder.addLong("codePostal", codePostalInput);
				}
				if (codeInseeInput != null) {
					builder.addLong("codeInsee", codeInseeInput);
				}
				JobParameters params = builder.toJobParameters();
				launcher.start(banBatchJob, params);


				//Fin traitement et durée traitement
				LocalDateTime end = LocalDateTime.now();
				Duration duree = Duration.between(start, end);
				long minutes = duree.toMinutes();
				long secondes = duree.minusMinutes(minutes).getSeconds();
				logs.write(String.format("Durée insertion  : %d min %d sec", minutes, secondes));

				//Demande a l'utilisateur si il a d'autre traitement a faire
				String res;
				do {
					res = scanner.getString("Voulez-vous traiter un autre fichier ? (y/n) : ").toLowerCase();
				} while (!res.equals("y") && !res.equals("n"));
				if (res.equals("n")){
					System.out.println("Fin de traitement des fichiers ...");
					isNotFinished = false;
				}
			} while (isNotFinished);
			logs.write("============================  Fin logs   ============================");
			logs.close();
			scanner.close();
		};
	}
}
