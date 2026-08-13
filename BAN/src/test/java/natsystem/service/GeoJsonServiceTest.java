package natsystem.service;

import natsystem.api.repository.GeoJsonRepository;
import natsystem.api.service.GeoJsonService;
import natsystem.geojson.entity.CommunesGeoJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoJsonServiceTest {
    @Mock
    private GeoJsonRepository repo;

    @InjectMocks
    private GeoJsonService geoJsonService;

    private CommunesGeoJson communesGeoJson(String codeInsee) {
        CommunesGeoJson communesGeoJson = new CommunesGeoJson();
        communesGeoJson.setCodeInsee(codeInsee);
        communesGeoJson.setDepartement("75");
        communesGeoJson.setEpci("Île-de-France");
        communesGeoJson.setGeometry("...");
        communesGeoJson.setNom("Paris");
        communesGeoJson.setRegion("Métropole du Grand Paris");
        return communesGeoJson;
    }

    @Test
    void findByCodeInsee_shouldReturnCommune() {
        String codeInsee = "75056";
        CommunesGeoJson expected = communesGeoJson(codeInsee);
        when(repo.findByCodeInsee(codeInsee)).thenReturn(expected);

        CommunesGeoJson result = geoJsonService.findByCodeInsee(codeInsee);

        assertSame(expected, result);
        verify(repo).findByCodeInsee(codeInsee);
    }

    @Test
    void findByCodeInsee_shouldReturnNull_whenCommuneDoesNotExist() {
        String codeInsee = "99999";
        when(repo.findByCodeInsee(codeInsee)).thenReturn(null);

        CommunesGeoJson result = geoJsonService.findByCodeInsee(codeInsee);

        assertSame(null, result);
        verify(repo).findByCodeInsee(codeInsee);
    }
}
