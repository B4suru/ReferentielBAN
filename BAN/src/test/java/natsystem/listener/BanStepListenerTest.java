package natsystem.listener;

import natsystem.ban.batch.context.BanDiffContext;
import natsystem.ban.batch.listener.BanStepListener;
import natsystem.shared.tools.FileManager;
import natsystem.shared.tools.Tool;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BanStepListenerTest {
    @Mock
    private FileManager rapport;

    @Mock
    private FileManager logs;

    @Mock
    private BanDiffContext banDiffContext;

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private IndexWriter indexWriter;

    private JobExecution jobExecution;
    private BanStepListener listener;
    private MockedStatic<Tool> toolMock;

    @BeforeEach
    void setUp() {
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        jobExecution = new JobExecution(1L, jobInstance, new JobParametersBuilder().toJobParameters());

        listener = new BanStepListener(rapport, banDiffContext, jdbc, indexWriter, logs);

        lenient().when(banDiffContext.getBdMap()).thenReturn(new LinkedHashMap<>());
        lenient().when(banDiffContext.getInsertCount()).thenReturn(new AtomicInteger(0));
        lenient().when(banDiffContext.getUpdateCount()).thenReturn(new AtomicInteger(0));

        toolMock = mockStatic(Tool.class);
    }

    @AfterEach
    void tearDown() {
        toolMock.close();
    }

    private StepExecution buildStepExecution(String stepName) {
        StepExecution step = new StepExecution(1, stepName, jobExecution);
        step.getExecutionContext().putString("file", "/fake/csv/data.csv");
        step.setExitStatus(ExitStatus.COMPLETED);
        return step;
    }

    // ---- beforeReport : purge DB + Lucene ----

    @Test
    void beforeReport_shouldDeleteFromDbAndLucene_forEachEntryInBdMap() throws IOException {
        Map<String, String> bdMap = new LinkedHashMap<>();
        bdMap.put("id1", "hash1");
        bdMap.put("id2", "hash2");
        when(banDiffContext.getBdMap()).thenReturn(bdMap);

        listener.afterStep(buildStepExecution("banBatchStep:0"));

        verify(jdbc).update("DELETE FROM ban WHERE id = ?", "id1");
        verify(jdbc).update("DELETE FROM ban WHERE id = ?", "id2");
        verify(indexWriter).deleteDocuments(new Term("id", "id1"));
        verify(indexWriter).deleteDocuments(new Term("id", "id2"));
        verify(logs).write("Supprimé : id1");
        verify(logs).write("Supprimé : id2");
        verify(indexWriter).commit();
    }

    @Test
    void beforeReport_shouldStillCommit_whenBdMapIsEmpty() throws IOException {
        listener.afterStep(buildStepExecution("banBatchStep:0"));

        verify(jdbc, never()).update(anyString(), anyString());
        verify(indexWriter, never()).deleteDocuments(any(Term.class));
        verify(indexWriter).commit();
    }

    @Test
    void beforeReport_shouldStillLogAndCommit_whenLuceneDeleteThrows() throws IOException {
        Map<String, String> bdMap = new LinkedHashMap<>();
        bdMap.put("id1", "hash1");
        when(banDiffContext.getBdMap()).thenReturn(bdMap);
        when(indexWriter.deleteDocuments(any(Term.class))).thenThrow(new IOException("erreur lucene"));

        listener.afterStep(buildStepExecution("banBatchStep:0"));

        // le log de suppression a lieu même si la suppression Lucene a échoué
        verify(logs).write("Supprimé : id1");
        verify(jdbc).update("DELETE FROM ban WHERE id = ?", "id1");
        verify(indexWriter).commit();
    }

    @Test
    void beforeReport_shouldNotThrow_whenCommitThrows() throws IOException {
        when(indexWriter.commit()).thenThrow(new IOException("commit impossible"));

        assertThatCode(() -> listener.afterStep(buildStepExecution("banBatchStep:0")))
                .doesNotThrowAnyException();
    }

    // ---- afterReport : suppression du fichier temporaire ----

    @Test
    void afterReport_shouldDeleteTempFile_usingFileFromExecutionContext() {
        StepExecution step = buildStepExecution("banBatchStep:0");
        step.getExecutionContext().putString("file", "/fake/csv/specific-file.csv");

        listener.afterStep(step);

        toolMock.verify(() -> Tool.deleteTempFile("/fake/csv/specific-file.csv"));
    }

    // ---- writeCustomReport ----

    @Test
    void writeCustomReport_shouldStoreCountsInStepExecutionContext() {
        when(banDiffContext.getInsertCount()).thenReturn(new AtomicInteger(42));
        when(banDiffContext.getUpdateCount()).thenReturn(new AtomicInteger(7));
        Map<String, String> bdMap = new LinkedHashMap<>();
        bdMap.put("id1", "hash1");
        bdMap.put("id2", "hash2");
        bdMap.put("id3", "hash3");
        when(banDiffContext.getBdMap()).thenReturn(bdMap);

        StepExecution step = buildStepExecution("banBatchStep:0");
        listener.afterStep(step);

        assertThat(step.getExecutionContext().getLong("nbInserted")).isEqualTo(42);
        assertThat(step.getExecutionContext().getLong("nbUpdated")).isEqualTo(7);
        assertThat(step.getExecutionContext().getLong("nbDeleted")).isEqualTo(3);
    }

    @Test
    void writeCustomReport_shouldWriteFormattedLines_toRapport() {
        when(banDiffContext.getInsertCount()).thenReturn(new AtomicInteger(42));
        when(banDiffContext.getUpdateCount()).thenReturn(new AtomicInteger(7));

        listener.afterStep(buildStepExecution("banBatchStep:0"));

        verify(rapport).write("Insertions       : 42");
        verify(rapport).write("Mises à jour     : 7");
        verify(rapport).write("Suppressions     : 0");
    }

    // ---- comportement hérité du template (sanity check) ----

    @Test
    void afterStep_shouldReturnStepExecutionExitStatus_unchanged() {
        StepExecution step = buildStepExecution("banBatchStep:0");
        ExitStatus failed = new ExitStatus("FAILED", "erreur de test");
        step.setExitStatus(failed);

        ExitStatus result = listener.afterStep(step);

        assertThat(result).isEqualTo(failed);
    }

}
