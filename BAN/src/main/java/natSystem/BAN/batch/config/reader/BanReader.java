package natSystem.BAN.batch.config.reader;

import natSystem.BAN.entity.Ban;
import natSystem.BAN.tools.ParseTool;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class BanReader {

    @Bean
    @StepScope
    public FlatFileItemReader<Ban> csvReader() {
        return new FlatFileItemReaderBuilder<Ban>()
                .name("banCsvReader")
                .resource(new FileSystemResource("csv_sorted.csv"))
                .delimited()
                .delimiter(";")
                .strict(false)
                .names("id",
                        "id_fantoir",
                        "numero",
                        "rep",
                        "nom_voie",
                        "code_postal",
                        "code_insee",
                        "nom_commune",
                        "code_insee_ancienne_commune",
                        "nom_ancienne_commune",
                        "x",
                        "y",
                        "lon",
                        "lat",
                        "type_position",
                        "alias",
                        "nom_ld",
                        "libelle_acheminement",
                        "nom_afnor",
                        "source_position",
                        "source_nom_voie",
                        "certification_commune",
                        "cad_parcelles",
                        "id_ban_adresse",
                        "id_ban_toponyme",
                        "id_ban_commune"
                )
                .fieldSetMapper(fs -> {
                    Ban b = new Ban();
                    ParseTool parseTool = new ParseTool();
                    b.setId(fs.readString("id"));
                    b.setNumero(parseTool.parseIntSafe(fs.readString("numero")));
                    b.setRep(fs.readString("rep"));
                    b.setNomVoie(fs.readString("nom_voie"));
                    b.setCodePostal(parseTool.parseIntSafe(fs.readString("code_postal")));
                    b.setCodeInsee(parseTool.parseIntSafe(fs.readString("code_insee")));
                    b.setNomCommune(fs.readString("nom_commune"));
                    b.setX(parseTool.parseDoubleSafe(fs.readString("x")));
                    b.setY(parseTool.parseDoubleSafe(fs.readString("y")));
                    b.setLon(parseTool.parseDoubleSafe(fs.readString("lon")));
                    b.setLat(parseTool.parseDoubleSafe(fs.readString("lat")));
                    return b;})
                .linesToSkip(1)
                .build();
    }

}
