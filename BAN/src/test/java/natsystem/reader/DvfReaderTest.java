package natsystem.reader;
import natsystem.dvf.batch.config.reader.DvfReader;
import natsystem.dvf.entity.Dvf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DvfReaderTest {

    private static final String HEADER = String.join(",",
            "id_mutation", "date_mutation", "numero_disposition", "nature_mutation",
            "valeur_fonciere", "adresse_numero", "adresse_suffixe", "adresse_nom_voie",
            "adresse_code_voie", "code_postal", "code_commune", "nom_commune",
            "code_departement", "ancien_code_commune", "ancien_nom_commune", "id_parcelle",
            "ancien_id_parcelle", "numero_volume", "lot1_numero", "lot1_surface_carrez",
            "lot2_numero", "lot2_surface_carrez", "lot3_numero", "lot3_surface_carrez",
            "lot4_numero", "lot4_surface_carrez", "lot5_numero", "lot5_surface_carrez",
            "nombre_lots", "code_type_local", "type_local", "surface_reelle_bati",
            "nombre_pieces_principales", "code_nature_culture", "nature_culture",
            "code_nature_culture_speciale", "nature_culture_speciale", "surface_terrain",
            "longitude", "latitude");

    private final DvfReader dvfReaderConfig = new DvfReader();
    private FlatFileItemReader<Dvf> reader;

    @AfterEach
    void tearDown() {
        if (reader != null) {
            reader.close();
        }
    }

    private FlatFileItemReader<Dvf> openReaderOn(Path csvFile) throws Exception {
        reader = dvfReaderConfig.dvfCsvReader(csvFile.toString());
        reader.open(new ExecutionContext());
        return reader;
    }

    private String validLine(String idMutation, String dateMutation) {
        return String.join(",",
                idMutation, dateMutation, "1", "Vente", "250000", "12", "", "RUE DE LA PAIX",
                "1234", "75002", "75102", "Paris", "75", "", "", "751020000AB0001",
                "", "", "1", "45.5", "", "", "", "",
                "", "", "", "",
                "1", "2", "Appartement", "45",
                "2", "", "", "", "", "",
                "2.3315", "48.8697");
    }

    @Test
    void read_shouldMapFieldsCorrectly_forValidLine(@TempDir Path tempDir) throws Exception {
        Path csvFile = writeCsv(tempDir, HEADER, validLine("2024-1", "2024-03-15"));
        FlatFileItemReader<Dvf> r = openReaderOn(csvFile);

        Dvf item = r.read();

        assertThat(item).isNotNull();
        assertThat(item.getIdMutation()).isEqualTo("2024-1");
        assertThat(item.getDateMutation()).isEqualTo(LocalDate.of(2024, Month.MARCH, 15));
        assertThat(item.getNumeroDisposition()).isEqualTo(1);
        assertThat(item.getNatureMutation()).isEqualTo("Vente");
        assertThat(item.getValeurFonciere()).isEqualTo(250000.0);
        assertThat(item.getAdresseNumero()).isEqualTo(12);
        assertThat(item.getAdresseNomVoie()).isEqualTo("RUE DE LA PAIX");
        assertThat(item.getCodePostal()).isEqualTo(75002);
        assertThat(item.getCodeCommune()).isEqualTo("75102");
        assertThat(item.getNomCommune()).isEqualTo("Paris");
        assertThat(item.getCodeDepartement()).isEqualTo("75");
        assertThat(item.getLot1SurfaceCarrez()).isEqualTo(45.5);
        assertThat(item.getNombreLots()).isEqualTo(1);
        assertThat(item.getCodeTypeLocal()).isEqualTo(2);
        assertThat(item.getTypeLocal()).isEqualTo("Appartement");
        assertThat(item.getSurfaceReelleBati()).isEqualTo(45);
        assertThat(item.getNombrePiecesPrincipales()).isEqualTo(2);
        assertThat(item.getLongitude()).isEqualTo(2.3315);
        assertThat(item.getLatitude()).isEqualTo(48.8697);

        assertThat(r.read()).isNull();
    }

    @Test
    void read_shouldSkipHeaderLine(@TempDir Path tempDir) throws Exception {
        Path csvFile = writeCsv(tempDir, HEADER, validLine("2024-1", "2024-03-15"));
        FlatFileItemReader<Dvf> r = openReaderOn(csvFile);

        Dvf item = r.read();

        assertThat(item.getIdMutation()).isNotEqualTo("id_mutation");
    }

    @Test
    void read_shouldReturnMultipleItems_inFileOrder(@TempDir Path tempDir) throws Exception {
        Path csvFile = writeCsv(tempDir, HEADER,
                validLine("2024-1", "2024-03-15"),
                validLine("2024-2", "2024-04-20"));
        FlatFileItemReader<Dvf> r = openReaderOn(csvFile);

        assertThat(r.read().getIdMutation()).isEqualTo("2024-1");
        assertThat(r.read().getIdMutation()).isEqualTo("2024-2");
        assertThat(r.read()).isNull();
    }

    @Test
    void read_shouldMapInvalidNumericFieldsToNull(@TempDir Path tempDir) throws Exception {
        String line = String.join(",",
                "2024-3", "2024-03-15", "NOT_A_NUMBER", "Vente", "INVALID", "12", "", "RUE C",
                "1234", "75002", "75102", "Paris", "75", "", "", "751020000AB0002",
                "", "", "", "",
                "", "", "", "",
                "", "", "", "",
                "1", "2", "Appartement", "45",
                "2", "", "", "", "", "",
                "2.3315", "48.8697");

        Path csvFile = writeCsv(tempDir, HEADER, line);
        FlatFileItemReader<Dvf> r = openReaderOn(csvFile);

        Dvf item = r.read();

        assertThat(item).isNotNull();
        assertThat(item.getNumeroDisposition()).isNull();
        assertThat(item.getValeurFonciere()).isEqualTo(0.0);
        assertThat(item.getAdresseNumero()).isEqualTo(12);
        assertThat(item.getLongitude()).isEqualTo(2.3315);
    }

    @Test
    void read_shouldThrowFlatFileParseException_onBlankDateMutation(@TempDir Path tempDir) throws Exception {
        Path csvFile = writeCsv(tempDir, HEADER, validLine("2024-4", ""));
        FlatFileItemReader<Dvf> r = openReaderOn(csvFile);

        assertThatThrownBy(r::read)
                .isInstanceOf(FlatFileParseException.class);
    }

    private Path writeCsv(Path dir, String... lines) throws IOException {
        Path file = dir.resolve("dvf_test.csv");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }
}