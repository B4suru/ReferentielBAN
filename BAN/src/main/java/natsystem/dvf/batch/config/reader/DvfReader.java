package natsystem.dvf.batch.config.reader;

import natsystem.dvf.entity.Dvf;
import natsystem.shared.tools.Tool;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.time.LocalDate;

@Configuration
public class DvfReader {

    @Bean
    @StepScope
    public FlatFileItemReader<Dvf> dvfCsvReader(@Value("#{stepExecutionContext['file']}") String file) {
        return new FlatFileItemReaderBuilder<Dvf>()
            .name("dvfCsvReader")
            .resource(new FileSystemResource(file))
            .delimited()
            .delimiter(",")
            .strict(false)
            .names("id_mutation",
                    "date_mutation",
                    "numero_disposition",
                    "nature_mutation",
                    "valeur_fonciere",
                    "adresse_numero",
                    "adresse_suffixe",
                    "adresse_nom_voie","adresse_code_voie","code_postal",
                    "code_commune",
                    "nom_commune",
                    "code_departement",
                    "ancien_code_commune",
                    "ancien_nom_commune",
                    "id_parcelle",
                    "ancien_id_parcelle",
                    "numero_volume",
                    "lot1_numero",
                    "lot1_surface_carrez",
                    "lot2_numero",
                    "lot2_surface_carrez",
                    "lot3_numero",
                    "lot3_surface_carrez",
                    "lot4_numero",
                    "lot4_surface_carrez",
                    "lot5_numero",
                    "lot5_surface_carrez",
                    "nombre_lots",
                    "code_type_local",
                    "type_local",
                    "surface_reelle_bati",
                    "nombre_pieces_principales",
                    "code_nature_culture",
                    "nature_culture",
                    "code_nature_culture_speciale",
                    "nature_culture_speciale",
                    "surface_terrain",
                    "longitude",
                    "latitude")
                .fieldSetMapper(fs -> {
                    Dvf d = new Dvf();
                    Tool parseTool = new Tool();

                    d.setIdMutation(fs.readString("id_mutation"));
                    d.setDateMutation(LocalDate.parse(fs.readString("date_mutation")));
                    d.setNumeroDisposition(parseTool.parseIntSafe(fs.readString("numero_disposition")));
                    d.setNatureMutation(fs.readString("nature_mutation"));
                    d.setValeurFonciere(parseTool.parseDoubleSafe(fs.readString("valeur_fonciere")));
                    d.setAdresseNumero(parseTool.parseIntSafe(fs.readString("adresse_numero")));
                    d.setAdresseSuffixe(fs.readString("adresse_suffixe"));
                    d.setAdresseNomVoie(fs.readString("adresse_nom_voie"));
                    d.setAdresseCodeVoie(fs.readString("adresse_code_voie"));
                    d.setCodePostal(parseTool.parseIntSafe(fs.readString("code_postal")));
                    d.setCodeCommune(fs.readString("code_commune"));
                    d.setNomCommune(fs.readString("nom_commune"));
                    d.setCodeDepartement(fs.readString("code_departement"));
                    d.setAncienCodeCommune(fs.readString("ancien_code_commune"));
                    d.setAncienNomCommune(fs.readString("ancien_nom_commune"));
                    d.setIdParcelle(fs.readString("id_parcelle"));
                    d.setAncienIdParcelle(fs.readString("ancien_id_parcelle"));
                    d.setNumeroVolume(fs.readString("numero_volume"));
                    d.setLot1Numero(fs.readString("lot1_numero"));
                    d.setLot1SurfaceCarrez(parseTool.parseDoubleSafe(fs.readString("lot1_surface_carrez")));
                    d.setLot2Numero(fs.readString("lot2_numero"));
                    d.setLot2SurfaceCarrez(parseTool.parseDoubleSafe(fs.readString("lot2_surface_carrez")));
                    d.setLot3Numero(fs.readString("lot3_numero"));
                    d.setLot3SurfaceCarrez(parseTool.parseDoubleSafe(fs.readString("lot3_surface_carrez")));
                    d.setLot4Numero(fs.readString("lot4_numero"));
                    d.setLot4SurfaceCarrez(parseTool.parseDoubleSafe(fs.readString("lot4_surface_carrez")));
                    d.setLot5Numero(fs.readString("lot5_numero"));
                    d.setLot5SurfaceCarrez(parseTool.parseDoubleSafe(fs.readString("lot5_surface_carrez")));
                    d.setNombreLots(parseTool.parseIntSafe(fs.readString("nombre_lots")));
                    d.setCodeTypeLocal(parseTool.parseIntSafe(fs.readString("code_type_local")));
                    d.setTypeLocal(fs.readString("type_local"));
                    d.setSurfaceReelleBati(parseTool.parseIntSafe(fs.readString("surface_reelle_bati")));
                    d.setNombrePiecesPrincipales(parseTool.parseIntSafe(fs.readString("nombre_pieces_principales")));
                    d.setCodeNatureCulture(fs.readString("code_nature_culture"));
                    d.setNatureCulture(fs.readString("nature_culture"));
                    d.setCodeNatureCultureSpeciale(fs.readString("code_nature_culture_speciale"));
                    d.setNatureCultureSpeciale(fs.readString("nature_culture_speciale"));
                    d.setSurfaceTerrain(parseTool.parseIntSafe(fs.readString("surface_terrain")));
                    d.setLongitude(parseTool.parseDoubleSafe(fs.readString("longitude")));
                    d.setLatitude(parseTool.parseDoubleSafe(fs.readString("latitude")));
                    return d;
                })
            .linesToSkip(1)
            .build();
    }

}
