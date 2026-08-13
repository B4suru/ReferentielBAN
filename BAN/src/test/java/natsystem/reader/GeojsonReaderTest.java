package natsystem.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import natsystem.geojson.batch.config.reader.GeoJsonItemReader;
import natsystem.geojson.entity.CommunesGeoJson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class GeojsonReaderTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GeoJsonItemReader reader;

    @AfterEach
    void tearDown() {
        if (reader != null) {
            reader.close();
        }
    }

    private GeoJsonItemReader openReaderOn(String json) throws Exception {
        Resource resource = new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
        reader = new GeoJsonItemReader(resource);
        reader.open(new ExecutionContext());
        return reader;
    }

    private static final String TWO_FEATURES_GEOJSON = """
            {
              "type": "FeatureCollection",
              "crs": { "type": "name", "properties": { "name": "EPSG:4326" } },
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "code": "75056",
                    "nom": "Paris",
                    "departement": "75",
                    "region": "11",
                    "epci": "200054781"
                  },
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[2.3, 48.8], [2.4, 48.8], [2.4, 48.9], [2.3, 48.8]]]
                  }
                },
                {
                  "type": "Feature",
                  "properties": {
                    "code": "69123",
                    "nom": "Lyon",
                    "departement": "69",
                    "region": "84",
                    "epci": "200046977"
                  },
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[4.8, 45.7], [4.9, 45.7], [4.9, 45.8], [4.8, 45.7]]]
                  }
                }
              ]
            }
            """;

    @Test
    void read_shouldMapFirstFeatureCorrectly() throws Exception {
        GeoJsonItemReader r = openReaderOn(TWO_FEATURES_GEOJSON);

        CommunesGeoJson commune = r.read();

        assertThat(commune).isNotNull();
        assertThat(commune.getCodeInsee()).isEqualTo("75056");
        assertThat(commune.getNom()).isEqualTo("Paris");
        assertThat(commune.getDepartement()).isEqualTo("75");
        assertThat(commune.getRegion()).isEqualTo("11");
        assertThat(commune.getEpci()).isEqualTo("200054781");

        JsonNode expectedGeometry = MAPPER.readTree("""
                {"type": "Polygon", "coordinates": [[[2.3, 48.8], [2.4, 48.8], [2.4, 48.9], [2.3, 48.8]]]}
                """);
        assertThat(MAPPER.readTree(commune.getGeometry())).isEqualTo(expectedGeometry);
    }

    @Test
    void read_shouldReturnFeaturesInOrder_thenNullAtEnd() throws Exception {
        GeoJsonItemReader r = openReaderOn(TWO_FEATURES_GEOJSON);

        assertThat(r.read().getCodeInsee()).isEqualTo("75056");
        assertThat(r.read().getCodeInsee()).isEqualTo("69123");
        assertThat(r.read()).isNull();
    }

    @Test
    void read_shouldReturnNullImmediately_whenFeaturesArrayIsEmpty() throws Exception {
        String json = """
                {"type": "FeatureCollection", "features": []}
                """;
        GeoJsonItemReader r = openReaderOn(json);

        assertThat(r.read()).isNull();
    }

    @Test
    void open_shouldThrowItemStreamException_whenResourceCannotBeOpened() {
        Resource missingResource = new org.springframework.core.io.FileSystemResource(
                "/this/path/does/not/exist/whatsoever.json");
        GeoJsonItemReader r = new GeoJsonItemReader(missingResource);

        assertThatThrownBy(() -> r.open(new ExecutionContext()))
                .isInstanceOf(ItemStreamException.class)
                .hasCauseInstanceOf(java.io.IOException.class);
    }
}
