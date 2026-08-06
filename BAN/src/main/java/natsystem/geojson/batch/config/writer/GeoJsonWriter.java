package natsystem.geojson.batch.config.writer;

import natsystem.geojson.entity.CommunesGeoJson;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class GeoJsonWriter {
    @Bean
    public JdbcBatchItemWriter<CommunesGeoJson> jdbcWriterGeoJSON(DataSource ds) {
        return new JdbcBatchItemWriterBuilder<CommunesGeoJson>()
                .dataSource(ds)
                .sql("""
                        INSERT INTO communes
                        (
                            code_insee,
                            nom,
                            departement,
                            region,
                            epci,
                            geometry
                        )
                        VALUES
                        (
                            :codeInsee,
                            :nom,
                            :departement,
                            :region,
                            :epci,
                            :geometry
                        )
                        """)
                .beanMapped()
                .build();
    }
}
