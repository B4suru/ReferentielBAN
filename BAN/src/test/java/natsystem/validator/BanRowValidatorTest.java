package natsystem.validator;

import org.springframework.batch.infrastructure.item.validator.ValidationException;
import natsystem.ban.batch.validator.BanRowValidator;
import natsystem.ban.entity.Ban;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BanRowValidatorTest {

    private static final String VALID_ID = "AB123_XY1234_12345";

    @Mock
    private FileManager logsFileManager;

    private BanRowValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BanRowValidator(logsFileManager);
    }

    private Ban validBan(String id) {
        Ban ban = mock(Ban.class);
        lenient().when(ban.getId()).thenReturn(id);
        lenient().when(ban.getNumero()).thenReturn(12);
        lenient().when(ban.getNomVoie()).thenReturn("Rue de la Paix");
        lenient().when(ban.getCodePostal()).thenReturn(75002);
        lenient().when(ban.getCodeInsee()).thenReturn("75102");
        lenient().when(ban.getNomCommune()).thenReturn("Paris");
        lenient().when(ban.getX()).thenReturn(650123.45);
        lenient().when(ban.getY()).thenReturn(6862001.23);
        lenient().when(ban.getLon()).thenReturn(2.3315);
        lenient().when(ban.getLat()).thenReturn(48.8697);
        lenient().when(ban.getHash()).thenReturn("hash-" + id);
        return ban;
    }

    @Test
    void validate_shouldPass_whenAllFieldsAreValid() {
        Ban ban = validBan(VALID_ID);

        assertThatCode(() -> validator.validate(ban)).doesNotThrowAnyException();
        verify(logsFileManager, never()).write(anyString());
    }

    @Test
    void validate_shouldThrow_whenIdIsNull() {
        Ban ban = validBan(VALID_ID);
        when(ban.getId()).thenReturn(null);

        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("L'id ne peut pas être vide");
        verify(logsFileManager).write("L'id ne peut pas être vide");
    }

    @Test
    void validate_shouldThrow_whenIdFormatIsInvalid() {
        Ban ban = validBan(VALID_ID);
        when(ban.getId()).thenReturn("format-invalide");

        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Format d'id invalide : format-invalide" );
        verify(logsFileManager).write("Format d'id invalide : format-invalide" );
    }

    @Test
    void validate_shouldThrow_whenNumeroIsLessThanZero() {
        Ban ban = validBan(VALID_ID);
        when(ban.getNumero()).thenReturn(-23);

        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le numero ne peux pas etre inférieur a 0 (id : " + VALID_ID + ")");
        verify(logsFileManager).write("Le numero ne peux pas etre inférieur a 0 (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenNomVoieIsNull() {
        Ban ban = validBan(VALID_ID);
        when(ban.getNomVoie()).thenReturn(null);
        assertThatThrownBy(() -> validator.validate(ban))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Le nom de la voie ne peut pas être vide (id : " + VALID_ID + ")");
        verify(logsFileManager).write("Le nom de la voie ne peut pas être vide (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenCodePostalIsNull() {
        Ban ban = validBan(VALID_ID);
        when(ban.getCodePostal()).thenReturn(null);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le code postal est obligatoire (id : " + VALID_ID + ")");
        verify(logsFileManager).write("Le code postal est obligatoire (id : " + VALID_ID + ")");
    }


    @Test
    void validate_shouldThrow_whenCodeInseeIsEmpty() {
        Ban ban = validBan(VALID_ID);
        when(ban.getCodeInsee()).thenReturn(null);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le code insee est obligatoire (id : " + VALID_ID + ")");
        verify(logsFileManager).write("Le code insee est obligatoire (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenNomCommuneIsNull() {
        Ban ban = validBan(VALID_ID);
        when(ban.getNomCommune()).thenReturn(null);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Le nom de la commune ne peut pas être vide (id : " + VALID_ID + ")");
        verify(logsFileManager).write("Le nom de la commune ne peut pas être vide (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenXIsEmpty() {
        Ban ban = validBan(VALID_ID);
        when(ban.getX()).thenReturn(0.0);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La position X est obligatoire (id : " + VALID_ID + ")");
        verify(logsFileManager).write("La position X est obligatoire (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenYIsEmpty() {
        Ban ban = validBan(VALID_ID);
        when(ban.getY()).thenReturn(0.0);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La position Y est obligatoire (id : " + VALID_ID + ")");
        verify(logsFileManager).write("La position Y est obligatoire (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenLonIsEmpty() {
        Ban ban = validBan(VALID_ID);
        when(ban.getLon()).thenReturn(0.0);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La longitude est obligatoire (id : " + VALID_ID + ")");
        verify(logsFileManager).write("La longitude est obligatoire (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_whenLatIsEmpty() {
        Ban ban = validBan(VALID_ID);
        when(ban.getLat()).thenReturn(0.0);
        assertThatThrownBy(() -> validator.validate(ban))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La latitude est obligatoire (id : " + VALID_ID + ")");
        verify(logsFileManager).write("La latitude est obligatoire (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_onExactDuplicate_sameIdSameHash() throws Exception {
        Ban first = validBan(VALID_ID);
        validator.validate(first);

        Ban second = validBan(VALID_ID); // même id, même hash ("hash-" + VALID_ID)

        assertThatThrownBy(() -> validator.validate(second))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Doublon exact détecté, ligne ignorée (id : " + VALID_ID + ")");
        verify(logsFileManager).write("Doublon exact détecté, ligne ignorée (id : " + VALID_ID + ")");
    }

    @Test
    void validate_shouldThrow_onDuplicateWithDifferentValues_sameIdDifferentHash() throws Exception {
        Ban first = validBan(VALID_ID);
        validator.validate(first);

        Ban second = validBan(VALID_ID);
        when(second.getHash()).thenReturn("hash-different");
        when(second.compareValue(first)).thenReturn("[nomVoie: 'Rue A' -> 'Rue B']");

        assertThatThrownBy(() -> validator.validate(second))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Doublon avec valeurs différentes détecté (id : " + VALID_ID + ") [nomVoie: 'Rue A' -> 'Rue B']");
        verify(logsFileManager).write("Doublon avec valeurs différentes détecté (id : " + VALID_ID + ") [nomVoie: 'Rue A' -> 'Rue B']");
    }
}
