package natsystem.ban.batch.config.writer;

import natsystem.ban.entity.Ban;
import natsystem.ban.lucene.config.writer.LuceneWriter;
import org.apache.lucene.index.IndexWriter;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class BanWriter {
    @Bean
    public JdbcBatchItemWriter<Ban> jdbcWriterBan(DataSource ds) {
        return new JdbcBatchItemWriterBuilder<Ban>()
                .dataSource(ds)
                .sql("""
                        INSERT INTO ban (
                            id,
                            numero,
                            rep,
                            nom_voie,
                            code_postal,
                            code_insee,
                            nom_commune,
                            x,
                            y,
                            lon,
                            lat,
                            hash
                        )
                        VALUES (
                            :id,
                            :numero,
                            :rep,
                            :nomVoie,
                            :codePostal,
                            :codeInsee,
                            :nomCommune,
                            :x,
                            :y,
                            :lon,
                            :lat,
                            :hash
                        ) ON CONFLICT (id) DO UPDATE SET
                            numero = EXCLUDED.numero,
                            rep = EXCLUDED.rep,
                            nom_voie = EXCLUDED.nom_voie,
                            code_postal = EXCLUDED.code_postal,
                            code_insee = EXCLUDED.code_insee,
                            nom_commune = EXCLUDED.nom_commune,
                            x = EXCLUDED.x,
                            y = EXCLUDED.y,
                            lon = EXCLUDED.lon,
                            lat = EXCLUDED.lat,
                            hash = EXCLUDED.hash;
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    public LuceneWriter luceneWriter(IndexWriter indexWriter) {
        return new LuceneWriter(indexWriter);
    }

    @Bean
    public ItemWriter<Ban> compositeWriter(JdbcBatchItemWriter<Ban> jdbcWriterBan,
                                           LuceneWriter luceneWriter) {
        CompositeItemWriter<Ban> composite = new CompositeItemWriter<>();
        composite.setDelegates(List.of(jdbcWriterBan, luceneWriter));
        return composite;
    }
}
