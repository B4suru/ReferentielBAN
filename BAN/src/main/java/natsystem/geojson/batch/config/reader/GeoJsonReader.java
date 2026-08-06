package natsystem.geojson.batch.config.reader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class GeoJsonReader {
    @Bean(destroyMethod = "") //Ferme deux fois le fichier sinon
    @StepScope
    public GeoJsonItemReader communesGeoJsonReader(
            @Value("#{jobExecutionContext['file']}") String file) {
        return new GeoJsonItemReader(new FileSystemResource(file));
    }
}
