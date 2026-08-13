package natsystem.geojson.batch.config.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import natsystem.geojson.entity.CommunesGeoJson;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.core.io.Resource;
import java.io.IOException;

@Slf4j
public class GeoJsonItemReader implements ItemStreamReader<CommunesGeoJson> {
    private JsonParser parser;
    private ObjectMapper mapper = new ObjectMapper();
    private final Resource resource;

    public GeoJsonItemReader(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            parser = mapper.getFactory().createParser(resource.getInputStream());

            while (parser.nextToken() != JsonToken.FIELD_NAME
                    || !"features".equals(parser.currentName())) {
                //Ne rien faire tant que l'on n'a pas attein la partie "features" du fichier geojson
            }

            parser.nextToken();
        }catch (IOException e){
            throw new ItemStreamException("Error opening GeoJsonItemReader",e);
        }
    }

    @Override
    public CommunesGeoJson read() throws Exception {
        JsonToken token = parser.nextToken();

        if (token  == JsonToken.END_ARRAY) {
            return null;
        }
        JsonNode feature = mapper.readTree(parser);
        JsonNode properties = feature.get("properties");

        CommunesGeoJson commune = new CommunesGeoJson();
        commune.setCodeInsee(properties.path("code").asText());
        commune.setNom(properties.path("nom").asText());
        commune.setDepartement(properties.path("departement").asText());
        commune.setRegion(properties.path("region").asText());
        commune.setEpci(properties.path("epci").asText());
        commune.setGeometry(feature.get("geometry").toString());

        return commune;
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (parser != null) {
                parser.close();
            }
        } catch (IOException e) {
            log.error("Error closing parser : " + e.getMessage());
        }
    }
}
