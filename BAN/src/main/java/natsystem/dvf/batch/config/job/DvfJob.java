package natsystem.dvf.batch.config.job;

import natsystem.shared.listener.JobListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DvfJob {

    @Bean
    public Job jobImportDvf(JobRepository jobRepository, Step retrieveDvfCsvStep, Step dvfSplitStep, Step dvfMasterStep, JobListener listener) {
        return new JobBuilder("dvfBatchJob", jobRepository)
                .listener(listener)
                .start(retrieveDvfCsvStep).on("NO_INPUT_FILE").end("NO_INPUT_FILE")
                .from(retrieveDvfCsvStep).on("MULTIPLE_FILES_FOUND").fail()
                .from(retrieveDvfCsvStep).on("CSV_NOT_VALID").fail()
                .from(retrieveDvfCsvStep).on("*").to(dvfSplitStep)
                .next(dvfMasterStep)
                .end()
                .build();
    }
}
