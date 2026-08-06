package natsystem.ban.batch.config.step;

import natsystem.ban.batch.config.tasklet.DeleteUnusedDeptTasklet;
import natsystem.ban.batch.config.tasklet.RetrieveBanFileTasklet;
import natsystem.ban.batch.config.tasklet.SplitDepartmentTasklet;
import natsystem.ban.batch.listener.BanStepListener;
import natsystem.shared.listener.MasterStepListener;
import natsystem.shared.partitioner.DepartementPartitioner;
import natsystem.ban.entity.Ban;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class BanStep {
    @Bean
    public Step banBatchStep(JobRepository jobRepository, PlatformTransactionManager txtManager,
                             FlatFileItemReader<Ban> banCsvReader,
                             CompositeItemProcessor<Ban, Ban> banCompositeItemProcessor,
                             ItemWriter<Ban> compositeWriter,
                             BanStepListener listener) {
        return new StepBuilder("banBatchStep", jobRepository)
                .<Ban, Ban>chunk(5000)
                .transactionManager(txtManager)
                .reader(banCsvReader)
                .processor(banCompositeItemProcessor)
                .writer(compositeWriter)
                .listener(listener)
                .build();
    }


    @Bean
    public Step banSplitStep(JobRepository jobRepository, PlatformTransactionManager txtManager, SplitDepartmentTasklet tasklet) {
        return new StepBuilder("splitStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step deleteUnusedDeptStep(JobRepository jobRepository, PlatformTransactionManager txtManager, DeleteUnusedDeptTasklet tasklet) {
        return  new StepBuilder("deleteUnusedDeptStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step retrieveBanCsvStep(JobRepository jobRepository, PlatformTransactionManager txtManager, RetrieveBanFileTasklet tasklet) {
        return  new StepBuilder("retrieveCsvStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }

    @Bean
    public Step banMasterStep(JobRepository jobRepository,
                              Step banBatchStep,
                              DepartementPartitioner partitioner,
                              @Value("${batch.partition.threads}") int threads,
                              MasterStepListener listener,
                              @Qualifier("applicationTaskExecutor") TaskExecutor applicationTaskExecutor
    ) {
        return new StepBuilder("masterStep", jobRepository)
                .listener(listener)
                .partitioner("banBatchStep", partitioner)
                .step(banBatchStep)
                .gridSize(threads)
                .taskExecutor(applicationTaskExecutor)
                .build();
    }
}
