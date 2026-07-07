package natSystem.BAN.lucene.listerner;

import org.apache.lucene.index.IndexWriter;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

import java.io.IOException;

public class LuceneCloseListener implements JobExecutionListener {
    private final IndexWriter indexWriter;

    public LuceneCloseListener(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            indexWriter.commit();
            indexWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erreur fermeture index Lucene", e);
        }
    }
}
