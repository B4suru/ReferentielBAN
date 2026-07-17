package natSystem.BAN.batch.config.step;

import natSystem.BAN.batch.config.tasklet.RetrieveCsvTasklet;
import natSystem.BAN.batch.config.tasklet.SplitDepartmentTasklet;
import natSystem.BAN.batch.listener.BanStepListener;
import natSystem.BAN.batch.listener.MasterStepListener;
import natSystem.BAN.batch.partitioner.DepartementPartitioner;
import natSystem.BAN.entity.Ban;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class ConfigStep {
    @Bean
    public Step banBatchStep(JobRepository jobRepository, PlatformTransactionManager txtManager,
                        FlatFileItemReader<Ban> csvReader,
                        CompositeItemProcessor<Ban, Ban> compositeItemProcessor,
                        ItemWriter<Ban> compositeWriter,
                        BanStepListener listener) {
        return new StepBuilder("banBatchStep", jobRepository)
                .<Ban, Ban>chunk(5000)
                .transactionManager(txtManager)
                .reader(csvReader)
                .processor(compositeItemProcessor)
                .writer(compositeWriter)
                .listener(listener)
                .build();
    }


    @Bean
    public Step splitStep(JobRepository jobRepository, PlatformTransactionManager txtManager, SplitDepartmentTasklet tasklet) {
        return new StepBuilder("splitStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step retrieveCsvStep(JobRepository jobRepository, PlatformTransactionManager txtManager, RetrieveCsvTasklet tasklet) {
        return  new StepBuilder("retrieveCsvStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step masterStep(JobRepository jobRepository, Step banBatchStep, DepartementPartitioner partitioner, @Value("${batch.partition.threads}") int threads, MasterStepListener listener) {

        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("ban-partition-");
        taskExecutor.setConcurrencyLimit(threads);

        return new StepBuilder("masterStep", jobRepository)
                .listener(listener)
                .partitioner("banBatchStep", partitioner)
                .step(banBatchStep)
                .gridSize(threads)
                .taskExecutor(taskExecutor)
                .build();
    }
}
