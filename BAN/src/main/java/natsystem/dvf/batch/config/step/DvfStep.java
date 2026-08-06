package natsystem.dvf.batch.config.step;

import natsystem.dvf.batch.config.listener.DvfStepListener;
import natsystem.shared.listener.MasterStepListener;
import natsystem.shared.partitioner.DepartementPartitioner;
import natsystem.dvf.batch.config.tasklet.RetrieveDvfFileTasklet;
import natsystem.dvf.batch.config.tasklet.SplitDvfDepartmentTalklet;
import natsystem.dvf.entity.Dvf;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class DvfStep {
    @Bean
    public Step retrieveDvfCsvStep(JobRepository jobRepository, PlatformTransactionManager txtManager, RetrieveDvfFileTasklet tasklet) {
        return new StepBuilder("retrieveCsvStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step dvfSplitStep(JobRepository jobRepository, PlatformTransactionManager txtManager, SplitDvfDepartmentTalklet tasklet) {
        return new StepBuilder("splitStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step dvfMasterStep(JobRepository jobRepository, Step dvfBatchStep, DepartementPartitioner partitioner, @Value("${batch.partition.threads}") int threads, MasterStepListener listener) {

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("dvf-partition-");
        taskExecutor.setConcurrencyLimit(threads);

        return new StepBuilder("masterStep", jobRepository)
                .listener(listener)
                .partitioner("dvfBatchStep", partitioner)
                .step(dvfBatchStep)
                .gridSize(threads)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step dvfBatchStep(JobRepository jobRepository, PlatformTransactionManager txtManager,
                             FlatFileItemReader<Dvf> dvfCsvReader,
                             ItemWriter<Dvf> jdbcWriterDvf,
                             DvfStepListener listener,
                             ItemProcessor<Dvf, Dvf> dvfValidatingProcessor
                             ) {
        return new StepBuilder("dvfBatchStep", jobRepository)
                .<Dvf, Dvf>chunk(5000)
                .transactionManager(txtManager)
                .reader(dvfCsvReader)
                .processor(dvfValidatingProcessor)
                .writer(jdbcWriterDvf)
                .listener(listener)
                .build();
    }
}
