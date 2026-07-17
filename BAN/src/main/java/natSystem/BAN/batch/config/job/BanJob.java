package natSystem.BAN.batch.config.job;

import natSystem.BAN.batch.listener.JobListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BanJob {
    @Bean
    public Job banBatchJob(JobRepository jobRepository, Step masterStep, Step splitStep, Step retrieveCsvStep, JobListener listener) {
        return new JobBuilder("banBatchJob", jobRepository)
                .listener(listener)
                .start(retrieveCsvStep).on("NO_INPUT_FILE").end("NO_INPUT_FILE")
                .from(retrieveCsvStep).on("MULTIPLE_FILES_FOUND").fail()
                .from(retrieveCsvStep).on("CSV_NOT_VALID").fail()
                .from(retrieveCsvStep).on("*").to(splitStep)
                .next(masterStep)
                .end()
                .build();
    }
}
