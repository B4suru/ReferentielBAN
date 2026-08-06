package natsystem.ban.batch.config.job;

import natsystem.shared.listener.JobListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BanJob {
    @Bean
    public Job jobImportBan(JobRepository jobRepository, Step banMasterStep, Step banSplitStep, Step retrieveBanCsvStep, Step deleteUnusedDeptStep, JobListener listener) {
        return new JobBuilder("banBatchJob", jobRepository)
                .listener(listener)
                .start(retrieveBanCsvStep).on("NO_INPUT_FILE").end("NO_INPUT_FILE")
                .from(retrieveBanCsvStep).on("MULTIPLE_FILES_FOUND").fail()
                .from(retrieveBanCsvStep).on("CSV_NOT_VALID").fail()
                .from(retrieveBanCsvStep).on("*").to(banSplitStep)
                .next(deleteUnusedDeptStep)
                .next(banMasterStep)
                .end()
                .build();
    }
}
