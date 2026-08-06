package natsystem.geojson.batch.config.processor;

import natsystem.geojson.batch.validator.GeoJsonValidator;
import natsystem.geojson.entity.CommunesGeoJson;
import natsystem.shared.tools.FileManager;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoJsonProcessor {
    @Bean
    @StepScope
    public ValidatingItemProcessor<CommunesGeoJson> communesGeoJsonValidatingProcessor(
            FileManager logsFileManager){
        ValidatingItemProcessor<CommunesGeoJson> validator = new ValidatingItemProcessor<>(new GeoJsonValidator(logsFileManager));
        validator.setFilter(true);
        return validator;
    }
}
