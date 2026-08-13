package natsystem.processor;

import natsystem.geojson.batch.config.processor.GeoJsonProcessor;
import natsystem.geojson.entity.CommunesGeoJson;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GeoJsonProcessorTest {
    @Test
    void shouldCreateGeoJsonValidatingProcessor() {
        GeoJsonProcessor geoJsonProcessor = new GeoJsonProcessor();

        FileManager logsFileManager = mock(FileManager.class);

        ValidatingItemProcessor<CommunesGeoJson> processor =
                geoJsonProcessor.communesGeoJsonValidatingProcessor(logsFileManager);

        assertNotNull(processor);
    }
}
