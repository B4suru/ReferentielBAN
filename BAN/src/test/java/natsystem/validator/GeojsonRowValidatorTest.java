package natsystem.validator;

import natsystem.geojson.batch.validator.GeoJsonValidator;
import natsystem.geojson.entity.CommunesGeoJson;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.validator.ValidationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GeojsonRowValidatorTest {
    private static final String CODE_INSEE = "01001";

    @Mock
    private FileManager logsFileManager;

    private GeoJsonValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GeoJsonValidator(logsFileManager);
    }

    private CommunesGeoJson geojson(String codeInsee) {
        CommunesGeoJson geojson = mock(CommunesGeoJson.class);
        lenient().when(geojson.getCodeInsee()).thenReturn(codeInsee);
        lenient().when(geojson.getNom()).thenReturn("L'Abergement-Clémenciat");
        lenient().when(geojson.getDepartement()).thenReturn("01");
        lenient().when(geojson.getRegion()).thenReturn("84");
        lenient().when(geojson.getEpci()).thenReturn("200069193");
        lenient().when(geojson.getGeometry()).thenReturn("{\"type\":\"Polygon\",\"coordinates\":[[[4.9262,46.12],[4.9219,46.1204]]]}");

        return geojson;
    }

    @Test
    void validate_shouldPass_whenAllFieldsAreValid(){
        CommunesGeoJson geojson = geojson(CODE_INSEE);

        assertThatCode(() -> validator.validate(geojson)).doesNotThrowAnyException();
        verify(logsFileManager, never()).write(anyString());
    }

    @Test
    void validate_shouldThrow_whenCodeInseeIsNull(){
        CommunesGeoJson geojson = geojson(CODE_INSEE);
        when(geojson.getCodeInsee()).thenReturn(null);

        assertThatCode(() -> validator.validate(geojson))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le code insee ne peut pas être vide");
        verify(logsFileManager).write("Le code insee ne peut pas être vide");
    }

    @Test
    void validate_shouldThrow_whenGeometryIsNull(){
        CommunesGeoJson geojson = geojson(CODE_INSEE);
        when(geojson.getGeometry()).thenReturn(null);

        assertThatCode(() -> validator.validate(geojson))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le données géométrique ne peuvent pas être vide : " + CODE_INSEE);
        verify(logsFileManager).write("Le données géométrique ne peuvent pas être vide : " + CODE_INSEE);
    }
}
