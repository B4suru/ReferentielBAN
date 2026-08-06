package natsystem.api.service;

import lombok.AllArgsConstructor;
import natsystem.api.repository.GeoJsonRepository;
import natsystem.geojson.entity.CommunesGeoJson;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GeoJsonService {
    private final GeoJsonRepository repo;

    public CommunesGeoJson findByCodeInsee(String codeInsee) {
        return repo.findByCodeInsee(codeInsee);
    }
}
