package natSystem.BAN.batchConfig;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class HelloWorldBatchConfig {
	@Bean
	public Job helloWorldJob(JobRepository jobRepository, Step helloStep) {
		return new JobBuilder("helloWorldJob", jobRepository)
				.start(helloStep)
				.build();
	}
	
	@Bean
	public Step helloStep(JobRepository jobRepository, PlatformTransactionManager txtManager) {
		return new StepBuilder("helloStep", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					System.out.println("====== Hello, Spring Batch ! ======");
					return RepeatStatus.FINISHED;
				}, txtManager)
				.build();
	}
}
