package natsystem.geojson.batch.config.step;

import natsystem.geojson.batch.config.listener.GeoJsonStepListener;
import natsystem.geojson.batch.config.tasklet.RetrieveGeoJsonTasklet;
import natsystem.geojson.entity.CommunesGeoJson;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class GeoJsonStep {
    @Bean
    public Step retrieveGeoJSONStep(JobRepository jobRepository, PlatformTransactionManager txtManager, RetrieveGeoJsonTasklet tasklet) {
        return new StepBuilder("retrieveGeoJSONStep", jobRepository)
                .tasklet(tasklet, txtManager)
                .build();
    }


    @Bean
    public Step geoJSONBatchStep(JobRepository jobRepository,
                                 PlatformTransactionManager txtManager,
                                 ItemStreamReader<CommunesGeoJson> reader,
                                 ItemWriter<CommunesGeoJson> jdbcWriterGeoJSON,
                                 GeoJsonStepListener listener,
                                 ItemProcessor<CommunesGeoJson, CommunesGeoJson> communesGeoJsonValidatingProcessor) {
        return new StepBuilder("geoJSONBatchStep", jobRepository)
                .<CommunesGeoJson, CommunesGeoJson>chunk(5000)
                .transactionManager(txtManager)
                .reader(reader)
                .processor(communesGeoJsonValidatingProcessor)
                .writer(jdbcWriterGeoJSON)
                .listener(listener)
                .build();
    }
}
