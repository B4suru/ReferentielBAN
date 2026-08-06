package natsystem.ban.batch.listener;

import lombok.extern.slf4j.Slf4j;
import natsystem.ban.batch.context.BanDiffContext;
import natsystem.shared.listener.AbstractStepListener;
import natsystem.shared.tools.Tool;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import natsystem.shared.tools.FileManager;
import java.io.IOException;


@Slf4j
@Component
public class BanStepListener extends AbstractStepListener {
    private final BanDiffContext banDiffContext;
    private final JdbcTemplate jdbc;
    private final IndexWriter indexWriter;
    private final FileManager logs;


    public BanStepListener(
            FileManager rapportFileManager,
            BanDiffContext banDiffContext,
            JdbcTemplate jdbc,
            IndexWriter indexWriter,
            FileManager logsFileManager) {

        super(rapportFileManager);

        this.banDiffContext = banDiffContext;
        this.jdbc = jdbc;
        this.indexWriter = indexWriter;
        this.logs = logsFileManager;
    }


    @Override
    protected void beforeReport(StepExecution stepExecution) {

        for (String id : banDiffContext.getBdMap().keySet()) {
            jdbc.update("DELETE FROM ban WHERE id = ?", id);

            try {
                indexWriter.deleteDocuments(new Term("id", id));
            }
            catch (IOException e) {
                log.error("Erreur suppression Lucene pour {}", id, e);
            }
            logs.write("Supprimé : " + id);
        }

        try {
            indexWriter.commit();
        }
        catch (IOException e) {
            log.error("Erreur commit Lucene", e);
        }
    }

    @Override
    protected void afterReport(StepExecution stepExecution) {
        String file = stepExecution.getExecutionContext().getString("file");
        Tool.deleteTempFile(file);
    }


    @Override
    protected void writeCustomReport(StepExecution stepExecution) {
        int del = banDiffContext.getBdMap().size();

        stepExecution.getExecutionContext().putLong("nbInserted", banDiffContext.getInsertCount().get());
        stepExecution.getExecutionContext().putLong("nbUpdated", banDiffContext.getUpdateCount().get());
        stepExecution.getExecutionContext().putLong("nbDeleted", del);

        writeLine("Insertions       : " + banDiffContext.getInsertCount().get());
        writeLine("Mises à jour     : " + banDiffContext.getUpdateCount().get());
        writeLine("Suppressions     : " + del);
    }

}
