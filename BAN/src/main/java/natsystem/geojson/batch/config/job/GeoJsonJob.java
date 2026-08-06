package natsystem.geojson.batch.config.job;

import natsystem.shared.listener.JobListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoJsonJob {
    @Bean
    public Job jobImportGeoJSON(JobRepository jobRepository, Step retrieveGeoJSONStep, Step geoJSONBatchStep, JobListener listener) {
        return new JobBuilder("geoJSONBatchJob", jobRepository)
                .listener(listener)
                .start(retrieveGeoJSONStep).on("NO_INPUT_FILE").end("NO_INPUT_FILE")
                .from(retrieveGeoJSONStep).on("MULTIPLE_FILES_FOUND").fail()
                .from(retrieveGeoJSONStep).on("CSV_NOT_VALID").fail()
                .from(retrieveGeoJSONStep).on("*").to(geoJSONBatchStep)
                .end()
                .build();
    }
}
