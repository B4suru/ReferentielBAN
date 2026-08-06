package natsystem.ban.batch.config.tasklet;


import lombok.extern.slf4j.Slf4j;
import natsystem.shared.tools.TimerTool;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class DeleteUnusedDeptTasklet implements Tasklet {
    private final JdbcTemplate jdbc;
    private final IndexWriter indexWriter;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DeleteUnusedDeptTasklet(JdbcTemplate jdbc, IndexWriter indexWriter, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbc = jdbc;
        this.indexWriter = indexWriter;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        @SuppressWarnings("unchecked")
        Set<String> usedDepts = (Set<String>) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("usedDepts");

        Set<String> bdDepts = new HashSet<>(
                jdbc.queryForList("""
                        SELECT DISTINCT
                        CASE
                            WHEN LEFT(id, 2) IN ('97', '98') THEN LEFT(id, 3)
                           ELSE LEFT(id, 2)
                        END
                        FROM ban
                        """, String.class)
        );




        if (!bdDepts.isEmpty() && !usedDepts.isEmpty()) {
            bdDepts.removeAll(usedDepts);
            if (!bdDepts.isEmpty()) {
                Integer nbLigne = jdbc.queryForObject("select count(*) from ban", Integer.class);


                log.info("-- Suppression des départements inutilisés dans la bd --");
                TimerTool timerToolBd = new TimerTool();
                //Supression dans la BD
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("depts", bdDepts);
                namedParameterJdbcTemplate.update("""
                    DELETE FROM ban
                    WHERE (
                            CASE
                            WHEN LEFT(id, 2) IN ('97', '98') THEN LEFT(id, 3)
                            ELSE LEFT(id, 2)
                            END
                    ) IN (:depts);
                    """, params);
                log.info("Temps suppression : " + timerToolBd.showTimer());


                //Suppression dans l'index Lucene
                log.info("-- Suppression des départements inutilisés dans Lucene --");
                TimerTool timerToolLucene = new TimerTool();
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                for (String prefix : bdDepts) {
                    builder.add(
                            new PrefixQuery(new Term("id", prefix)),
                            BooleanClause.Occur.SHOULD
                    );
                }
                indexWriter.deleteDocuments(builder.build());
                indexWriter.commit();
                log.info("Temps suppression : " + timerToolLucene.showTimer());

                Integer nbLigneApresSuppression = jdbc.queryForObject("select count(*) from ban", Integer.class);
                log.info("Nombre de ligne suprimées : " + (nbLigne - nbLigneApresSuppression));
            }
        }
        return RepeatStatus.FINISHED;
    }
}
