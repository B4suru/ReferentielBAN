package natsystem.api.repository;

import natsystem.geojson.entity.CommunesGeoJson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeoJsonRepository extends JpaRepository<CommunesGeoJson, String> {
    CommunesGeoJson findByCodeInsee(String codeInsee);
}
