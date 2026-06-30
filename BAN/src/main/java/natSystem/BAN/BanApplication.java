package natSystem.BAN;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.tools.ScannerTool;
import natSystem.BAN.tools.TimerTool;
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
			log.info("-- Début du programme --");

			Path dossierLogs = Path.of("Logs");
			Files.createDirectories(dossierLogs);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSS");
			String logFileName = "Logs/Logs(" + LocalDateTime.now().format(formatter) + ").txt";
			FileManager logs = new FileManager(logFileName);
			logs.write("============================ Debuts logs ============================");
			ScannerTool scanner = new ScannerTool();

			boolean isNotFinished = true;
			do {
				log.info("-- Début séléction utilisateur --");

				//Recuperation du fichier csv
				FileManager csvFile = new FileManager();
				do {
					String cheminFichierCsv = scanner.getString("Chemin du fichier csv : ");
					csvFile.setFileName(cheminFichierCsv);
				} while (!csvFile.isCSVExisting() || csvFile.getFileName().isEmpty());

				//Récuperation des filtres
				Long codePostalInput = scanner.getLong("Code postal (laisser vide pour ignorer) : ");
				Long codeInseeInput = scanner.getLong("Code INSEE (laisser vide pour ignorer) : ");

				log.info("-- Fin séléction utilisateur --");

				//Début timer traitement
				TimerTool batchTimer = new TimerTool();
				logs.write("Date : " + batchTimer.getStart());
				logs.write("Fichier : " + csvFile.getFileName());
				logs.write("Filtre : [Code postal : " + codePostalInput + "] | [Code insee: " + codeInseeInput+ "]");

				//Récuperation de la taille du csv
				long sizeCSV = csvFile.countFileLine();


				//Tri du fichier csv
				log.info("-- Début tri csv --");
				TimerTool sortTimer = new TimerTool();
				csvFile.sortCSV();
				log.info("Temps tri csv : " + sortTimer.showTimer());
				logs.write("Temps tri csv : " + sortTimer.showTimer());
				log.info("-- Fin tri csv --");

				log.info("-- Début du traitement du fichier --");
				//Creation des paramètres et lancement du job
				JobParametersBuilder builder = new JobParametersBuilder()
						.addLong("startAt", System.currentTimeMillis())
						.addLong("sizeCSV", sizeCSV)
						.addString("logFileName", logFileName);
				if (codePostalInput != null) {
					builder.addLong("codePostal", codePostalInput);
				}
				if (codeInseeInput != null) {
					builder.addLong("codeInsee", codeInseeInput);
				}
				JobParameters params = builder.toJobParameters();
				launcher.start(banBatchJob, params);


				//Fin traitement et durée traitement
				logs.write("Temps tri csv : " + batchTimer.showTimer());
				log.info("-- Fin du traitement du fichier --");

				//Demande a l'utilisateur si il a d'autre traitement a faire
				String res;
				do {
					res = scanner.getString("Voulez-vous traiter un autre fichier ? (y/n) : ").toLowerCase();
				} while (!res.equals("y") && !res.equals("n"));
				if (res.equals("n")){
					System.out.println("Fin de traitement des fichiers ...");
					isNotFinished = false;
				}

				//Supprimer le fichier trier temporaire
				try {
					Path path = Path.of("csv_sorted.csv");
					Files.delete(path);
				}
				catch (IOException e) {
					System.err.println("Erreur : " + e.getMessage());
				}
			} while (isNotFinished);
			logs.write("============================  Fin logs   ============================");
			logs.close();
			scanner.close();
			log.info("-- Fin du programme --");
		};
	}
}
