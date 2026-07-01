package natSystem.BAN.batch.config.job;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BanJob {
    @Bean
    public Job banBatchJob(JobRepository jobRepository, Step banBatchStep) {
        return new JobBuilder("banBatchJob", jobRepository)
                .start(banBatchStep)
                .build();
    }
}
