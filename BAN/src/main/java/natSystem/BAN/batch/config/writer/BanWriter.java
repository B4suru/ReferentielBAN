package natSystem.BAN.batch.config.writer;

import natSystem.BAN.entity.Ban;
import natSystem.BAN.lucene.config.writer.LuceneWriter;
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
    public JdbcBatchItemWriter<Ban> jdbcWriter(DataSource ds) {
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
                            lat
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
                            :lat
                        );
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    public LuceneWriter luceneWriter(IndexWriter indexWriter) {
        return new LuceneWriter(indexWriter);
    }

    @Bean
    public ItemWriter<Ban> compositeWriter(JdbcBatchItemWriter<Ban> jdbcWriter,
                                           LuceneWriter luceneWriter) {
        CompositeItemWriter<Ban> composite = new CompositeItemWriter<>();
        composite.setDelegates(List.of(jdbcWriter, luceneWriter));
        return composite;
    }
}
