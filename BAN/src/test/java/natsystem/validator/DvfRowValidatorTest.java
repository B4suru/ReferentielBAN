package natsystem.validator;

import org.springframework.batch.infrastructure.item.validator.ValidationException;
import natsystem.dvf.batch.validator.DvfRowValidator;
import natsystem.dvf.entity.Dvf;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DvfRowValidatorTest {
    private static final String ID_MUTATION = "2024-1026242";

    @Mock
    private FileManager logsFileManager;

    private DvfRowValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DvfRowValidator(logsFileManager);
    }

    private Dvf validDvf(String id) {
        Dvf dvf = mock(Dvf.class);
        lenient().when(dvf.getIdMutation()).thenReturn(id);
        lenient().when(dvf.getDateMutation()).thenReturn(LocalDate.ofEpochDay(2024-03-15));
        lenient().when(dvf.getNumeroDisposition()).thenReturn(1);
        lenient().when(dvf.getNatureMutation()).thenReturn("Vente");
        lenient().when(dvf.getValeurFonciere()).thenReturn(250000.0);
        lenient().when(dvf.getAdresseNumero()).thenReturn(12);
        lenient().when(dvf.getAdresseSuffixe()).thenReturn("");
        lenient().when(dvf.getAdresseNomVoie()).thenReturn("RUE DE LA PAIX");
        lenient().when(dvf.getAdresseCodeVoie()).thenReturn("");
        lenient().when(dvf.getCodePostal()).thenReturn(75002);
        lenient().when(dvf.getCodeCommune()).thenReturn("75102");
        lenient().when(dvf.getNomCommune()).thenReturn("Paris");
        lenient().when(dvf.getCodeDepartement()).thenReturn("75");
        lenient().when(dvf.getAncienCodeCommune()).thenReturn("");
        lenient().when(dvf.getAncienNomCommune()).thenReturn("");
        lenient().when(dvf.getIdParcelle()).thenReturn("232");
        lenient().when(dvf.getNumeroVolume()).thenReturn("1");
        lenient().when(dvf.getLot1Numero()).thenReturn("1");
        lenient().when(dvf.getLot1SurfaceCarrez()).thenReturn(45.5);
        lenient().when(dvf.getLot2Numero()).thenReturn("");
        lenient().when(dvf.getLot2SurfaceCarrez()).thenReturn(null);
        lenient().when(dvf.getLot3Numero()).thenReturn("");
        lenient().when(dvf.getLot3SurfaceCarrez()).thenReturn(null);
        lenient().when(dvf.getLot4Numero()).thenReturn("");
        lenient().when(dvf.getLot4SurfaceCarrez()).thenReturn(null);
        lenient().when(dvf.getLot5Numero()).thenReturn("");
        lenient().when(dvf.getLot5SurfaceCarrez()).thenReturn(null);
        lenient().when(dvf.getNombreLots()).thenReturn(1);
        lenient().when(dvf.getTypeLocal()).thenReturn("Appartement");
        lenient().when(dvf.getSurfaceReelleBati()).thenReturn(45);
        lenient().when(dvf.getNombrePiecesPrincipales()).thenReturn(2);
        lenient().when(dvf.getCodeNatureCulture()).thenReturn("");
        lenient().when(dvf.getNatureCulture()).thenReturn("");
        lenient().when(dvf.getCodeNatureCultureSpeciale()).thenReturn("");
        lenient().when(dvf.getNatureCulture()).thenReturn("");
        lenient().when(dvf.getSurfaceTerrain()).thenReturn(null);
        lenient().when(dvf.getLongitude()).thenReturn(2.01112);
        lenient().when(dvf.getLatitude()).thenReturn(48.54656);

        return dvf;
    }

    @Test
    void validate_shouldPass_whenAllFieldsAreValid(){
        Dvf dvf = validDvf(ID_MUTATION);

        assertThatCode(() -> validator.validate(dvf)).doesNotThrowAnyException();
        verify(logsFileManager, never()).write(anyString());
    }

    @Test
    void validate_shouldThrow_whenIdMutationIsNull() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getIdMutation()).thenReturn(null);

       assertThatThrownBy(() -> validator.validate(dvf))
               .isInstanceOf(ValidationException.class)
               .hasMessage("L'id mutation ne peut pas être vide");

        verify(logsFileManager).write("L'id mutation ne peut pas être vide");
    }

    @Test
    void validate_shouldThrow_whenValeurFonciereIsEmpty() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getValeurFonciere()).thenReturn(0.0);

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La valeur foncière ne peut pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("La valeur foncière ne peut pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenAdresseNomVoieIsNull() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getAdresseNomVoie()).thenReturn(null);

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le nom de voie de l'adresse ne peut pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("Le nom de voie de l'adresse ne peut pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenCodePostalIsNull() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getCodePostal()).thenReturn(null);

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le code postal ne peut pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("Le code postal ne peut pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenCodeCommuneIsNull() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getCodeCommune()).thenReturn(null);

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le code de commune ne peut pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("Le code de commune ne peut pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenNomCommuneIsEmpty() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getNomCommune()).thenReturn("");

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le nom de la commune ne peut pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("Le nom de la commune ne peut pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenIdParcelleIsEmpty() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getIdParcelle()).thenReturn("");

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("L'id de la parcelle ne peut pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("L'id de la parcelle ne peut pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenLonIsNull() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getLongitude()).thenReturn(0.0);

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Les coordonnées ne peuvent pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("Les coordonnées ne peuvent pas être vide (id mutation : " + ID_MUTATION + ")");
    }

    @Test
    void validate_shouldThrow_whenLatIsEmpty() {
        Dvf dvf = validDvf(ID_MUTATION);
        when(dvf.getLatitude()).thenReturn(0.0);

        assertThatThrownBy(() -> validator.validate(dvf))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Les coordonnées ne peuvent pas être vide (id mutation : " + ID_MUTATION + ")");

        verify(logsFileManager).write("Les coordonnées ne peuvent pas être vide (id mutation : " + ID_MUTATION + ")");
    }
}
