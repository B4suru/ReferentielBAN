package natSystem.BAN;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run(JobLauncher launcher, Job banBatchJob) {
		return args -> {
			JobParameters params = new JobParametersBuilder()
					.addLong("startAt", System.currentTimeMillis())
					.toJobParameters();
			launcher.run(banBatchJob, params);
		};
	}
}
