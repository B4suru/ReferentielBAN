package natsystem.reader;


import natsystem.ban.batch.config.reader.BanReader;
import natsystem.ban.entity.Ban;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class BanReaderTest {

    private final BanReader banReaderConfig = new BanReader();
    private FlatFileItemReader<Ban> reader;

    @AfterEach
    void tearDown() {
        if (reader != null) {
            reader.close();
        }
    }

    private FlatFileItemReader<Ban> openReaderOn(Path csvFile) throws Exception {
        reader = banReaderConfig.banCsvReader(csvFile.toString());
        reader.open(new ExecutionContext());
        return reader;
    }

    @Test
    void read_shouldMapFieldsCorrectly_forValidLine(@TempDir Path tempDir) throws Exception {
        String header = "id;id_fantoir;numero;rep;nom_voie;code_postal;code_insee;nom_commune;" +
                "code_insee_ancienne_commune;nom_ancienne_commune;x;y;lon;lat;type_position;alias;" +
                "nom_ld;libelle_acheminement;nom_afnor;source_position;source_nom_voie;" +
                "certification_commune;cad_parcelles;id_ban_adresse;id_ban_toponyme;id_ban_commune";

        String dataLine = "ADR00001;9985A;12;B;Rue de la Paix;75002;75102;Paris;;;" +
                "650123.45;6862001.23;2.3315;48.8697;entree;;;PARIS;RUE DE LA PAIX;;;;;;;";

        Path csvFile = writeCsv(tempDir, header, dataLine);
        FlatFileItemReader<Ban> r = openReaderOn(csvFile);

        Ban item = r.read();

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo("ADR00001");
        assertThat(item.getNumero()).isEqualTo(12);
        assertThat(item.getRep()).isEqualTo("B");
        assertThat(item.getNomVoie()).isEqualTo("Rue de la Paix");
        assertThat(item.getCodePostal()).isEqualTo(75002);
        assertThat(item.getCodeInsee()).isEqualTo("75102");
        assertThat(item.getNomCommune()).isEqualTo("Paris");
        assertThat(item.getX()).isEqualTo(650123.45);
        assertThat(item.getY()).isEqualTo(6862001.23);
        assertThat(item.getLon()).isEqualTo(2.3315);
        assertThat(item.getLat()).isEqualTo(48.8697);

        // plus rien à lire ensuite
        assertThat(r.read()).isNull();
    }

    @Test
    void read_shouldSkipHeaderLine(@TempDir Path tempDir) throws Exception {
        String header = "id;id_fantoir;numero;rep;nom_voie;code_postal;code_insee;nom_commune;" +
                "code_insee_ancienne_commune;nom_ancienne_commune;x;y;lon;lat;type_position;alias;" +
                "nom_ld;libelle_acheminement;nom_afnor;source_position;source_nom_voie;" +
                "certification_commune;cad_parcelles;id_ban_adresse;id_ban_toponyme;id_ban_commune";

        String dataLine = "ADR00001;9985A;12;B;Rue de la Paix;75002;75102;Paris;;;" +
                "650123.45;6862001.23;2.3315;48.8697;entree;;;PARIS;RUE DE LA PAIX;;;;;;;";

        Path csvFile = writeCsv(tempDir, header, dataLine);
        FlatFileItemReader<Ban> r = openReaderOn(csvFile);

        Ban item = r.read();


        assertThat(item.getId()).isNotEqualTo("id");
        assertThat(item.getNumero()).isNotNull();
    }

    @Test
    void read_shouldReturnMultipleItems_inFileOrder(@TempDir Path tempDir) throws Exception {
        String header = "id;id_fantoir;numero;rep;nom_voie;code_postal;code_insee;nom_commune;" +
                "code_insee_ancienne_commune;nom_ancienne_commune;x;y;lon;lat;type_position;alias;" +
                "nom_ld;libelle_acheminement;nom_afnor;source_position;source_nom_voie;" +
                "certification_commune;cad_parcelles;id_ban_adresse;id_ban_toponyme;id_ban_commune";

        String line1 = "ADR00001;;12;;Rue A;75002;75102;Paris;;;600000;6862000;2.30;48.86;;;;;;;;;;;;";
        String line2 = "ADR00002;;13;;Rue B;75003;75103;Paris;;;600001;6862001;2.31;48.87;;;;;;;;;;;;";

        Path csvFile = writeCsv(tempDir, header, line1, line2);
        FlatFileItemReader<Ban> r = openReaderOn(csvFile);

        assertThat(r.read().getId()).isEqualTo("ADR00001");
        assertThat(r.read().getId()).isEqualTo("ADR00002");
        assertThat(r.read()).isNull();
    }


    @Test
    void read_shouldHandleInvalidNumericFields_gracefully(@TempDir Path tempDir) throws Exception {
        String header = "id;id_fantoir;numero;rep;nom_voie;code_postal;code_insee;nom_commune;" +
                "code_insee_ancienne_commune;nom_ancienne_commune;x;y;lon;lat;type_position;alias;" +
                "nom_ld;libelle_acheminement;nom_afnor;source_position;source_nom_voie;" +
                "certification_commune;cad_parcelles;id_ban_adresse;id_ban_toponyme;id_ban_commune";

        String dataLine = "ADR00003;;NOT_A_NUMBER;;Rue C;INVALID;75104;Paris;;;" +
                "NOT_A_DOUBLE;6862002;2.32;48.88;;;;;;;;;;;;";

        Path csvFile = writeCsv(tempDir, header, dataLine);
        FlatFileItemReader<Ban> r = openReaderOn(csvFile);

        Ban item = r.read();

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo("ADR00003");
        assertThat(item.getNumero()).isNull();
        assertThat(item.getCodePostal()).isNull();
        assertThat(item.getX()).isEqualTo(0.0);
        assertThat(item.getY()).isEqualTo(6862002.0);
    }

    private Path writeCsv(Path dir, String... lines) throws IOException {
        Path file = dir.resolve("ban_test.csv");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }
}