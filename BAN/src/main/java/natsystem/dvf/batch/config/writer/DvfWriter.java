package natsystem.dvf.batch.config.writer;

import natsystem.dvf.entity.Dvf;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DvfWriter {

    @Bean
    public JdbcBatchItemWriter<Dvf> jdbcWriterDvf(DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Dvf>()
                .dataSource(ds)
                .sql("""
                        INSERT INTO dvf (
                            id_mutation,
                            date_mutation,
                            numero_disposition,
                            nature_mutation,
                            valeur_fonciere,
                            adresse_numero,
                            adresse_suffixe,
                            adresse_nom_voie,
                            adresse_code_voie,
                            code_postal,
                            code_commune,
                            nom_commune,
                            code_departement,
                            ancien_code_commune,
                            ancien_nom_commune,
                            id_parcelle,
                            ancien_id_parcelle,
                            numero_volume,
                            lot1_numero,
                            lot1_surface_carrez,
                            lot2_numero,
                            lot2_surface_carrez,
                            lot3_numero,
                            lot3_surface_carrez,
                            lot4_numero,
                            lot4_surface_carrez,
                            lot5_numero,
                            lot5_surface_carrez,
                            nombre_lots,
                            code_type_local,
                            type_local,
                            surface_reelle_bati,
                            nombre_pieces_principales,
                            code_nature_culture,
                            nature_culture,
                            code_nature_culture_speciale,
                            nature_culture_speciale,
                            surface_terrain,
                            longitude,
                            latitude
                        )
                        VALUES (
                            :idMutation,
                            :dateMutation,
                            :numeroDisposition,
                            :natureMutation,
                            :valeurFonciere,
                            :adresseNumero,
                            :adresseSuffixe,
                            :adresseNomVoie,
                            :adresseCodeVoie,
                            :codePostal,
                            :codeCommune,
                            :nomCommune,
                            :codeDepartement,
                            :ancienCodeCommune,
                            :ancienNomCommune,
                            :idParcelle,
                            :ancienIdParcelle,
                            :numeroVolume,
                            :lot1Numero,
                            :lot1SurfaceCarrez,
                            :lot2Numero,
                            :lot2SurfaceCarrez,
                            :lot3Numero,
                            :lot3SurfaceCarrez,
                            :lot4Numero,
                            :lot4SurfaceCarrez,
                            :lot5Numero,
                            :lot5SurfaceCarrez,
                            :nombreLots,
                            :codeTypeLocal,
                            :typeLocal,
                            :surfaceReelleBati,
                            :nombrePiecesPrincipales,
                            :codeNatureCulture,
                            :natureCulture,
                            :codeNatureCultureSpeciale,
                            :natureCultureSpeciale,
                            :surfaceTerrain,
                            :longitude,
                            :latitude
                        );
                        """)
                .beanMapped()
                .build();
    }
}
