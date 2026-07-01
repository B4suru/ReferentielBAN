package natSystem.BAN.batch.config.step;

import natSystem.BAN.batch.listener.BanStepListener;
import natSystem.BAN.entity.Ban;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BanStep {
    @Bean
    public Step banBatchStep(JobRepository jobRepository, PlatformTransactionManager txtManager,
                        FlatFileItemReader<Ban> csvReader,
                        CompositeItemProcessor<Ban, Ban> compositeItemProcessor,
                        ItemWriter<Ban> writer,
                        BanStepListener listener) {
        return new StepBuilder("banBatchStep", jobRepository)
                .<Ban, Ban>chunk(5000)
                .transactionManager(txtManager)
                .reader(csvReader)
                .processor(compositeItemProcessor)
                .writer(writer)
                .listener(listener)
                .build();
    }
}
