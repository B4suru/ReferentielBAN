package natSystem.BAN;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import natSystem.BAN.file.File;

@SpringBootApplication
public class BanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run(JobLauncher launcher, Job banBatchJob) {
		return args -> {
			File logs = new File("Logs.txt");
			logs.write("============================ Debuts logs ============================");
			
			Scanner scanner = new Scanner(System.in);
			System.out.print("Code postal (laisser vide pour ignorer) : ");
			String codePostalInput = scanner.nextLine().trim();
			System.out.print("Code INSEE (laisser vide pour ignorer) : ");
			String codeInseeInput = scanner.nextLine().trim();
			
			LocalDateTime start = LocalDateTime.now();
			logs.write("Date : " + start);
			
			JobParametersBuilder builder = new JobParametersBuilder()
					.addLong("startAt", System.currentTimeMillis());

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
			logs.write("============================  Fin logs   ============================");
			
			scanner.close();
		};
	}
}
