package natSystem.BAN;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import natSystem.BAN.tools.file.File;


@SpringBootApplication
public class BanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run(JobOperator launcher, Job banBatchJob) {
		return args -> {
			File logs = new File("Logs.txt");
			logs.write("============================ Debuts logs ============================");
			Scanner scanner = new Scanner(System.in);
			
			boolean isNotFinished = true;
			
			do {
				
			
				File csvFile = new File();
				do {
					System.out.println("Chemin du fichier csv : ");
					String cheminFichierCsv = scanner.nextLine().trim();
					csvFile.setFileName(cheminFichierCsv);
				} while (!csvFile.isCSVExisting() || csvFile.getFileName().isEmpty());
				
				
				System.out.print("Code postal (laisser vide pour ignorer) : ");
				String codePostalInput = scanner.nextLine().trim();
				System.out.print("Code INSEE (laisser vide pour ignorer) : ");
				String codeInseeInput = scanner.nextLine().trim();
				
				LocalDateTime start = LocalDateTime.now();
				logs.write("Date : " + start);
				logs.write("Fichier : " + csvFile.getFileName());
				logs.write("Filtre : [Code postal : " + codePostalInput + "] | [Code insee: " + codeInseeInput+ "]");

				JobParametersBuilder builder = new JobParametersBuilder()
						.addLong("startAt", System.currentTimeMillis())
						.addString("cheminFichierCsv", csvFile.getFileName());
	
				if (!codePostalInput.isEmpty()) {
					try {
						builder.addLong("codePostal", Long.parseLong(codePostalInput));
					} catch (NumberFormatException e) {
						System.err.println("Code postal invalide, ignoré.");
					}
				}
				if (!codeInseeInput.isEmpty()) {
					try {
						builder.addLong("codeInsee", Long.parseLong(codeInseeInput));
					} catch (NumberFormatException e) {
						System.err.println("Code INSEE invalide, ignoré.");
					}
				}
	
				JobParameters params = builder.toJobParameters();
				launcher.run(banBatchJob, params);

				LocalDateTime end = LocalDateTime.now();
				Duration duree = Duration.between(start, end);
				long minutes = duree.toMinutes();
				long secondes = duree.minusMinutes(minutes).getSeconds();
				logs.write(String.format("Durée insertion : %d min %d sec", minutes, secondes));
				
				String res;
				do {
					System.out.print("Voulez-vous traiter un autre fichier ? (y/n) : ");
					res = scanner.nextLine().trim().toLowerCase();
				} while (!res.equals("y") && !res.equals("n"));

				if (res.equals("n")){
					System.out.println("Fin de traitement des fichiers ...");
					isNotFinished = false;
				}
			} while (isNotFinished);
			logs.write("============================  Fin logs   ============================");
			
			scanner.close();
		};
	}
}
