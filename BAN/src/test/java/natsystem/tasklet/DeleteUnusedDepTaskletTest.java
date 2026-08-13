package natsystem.tasklet;

import natsystem.ban.batch.config.tasklet.DeleteUnusedDeptTasklet;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUnusedDepTaskletTest {
    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private IndexWriter indexWriter;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private StepContribution contribution;

    private ExecutionContext jobExecutionContext;
    private ChunkContext chunkContext;
    private DeleteUnusedDeptTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new DeleteUnusedDeptTasklet(jdbc, indexWriter, namedParameterJdbcTemplate);

        jobExecutionContext = new ExecutionContext();
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(1L, jobInstance, new JobParametersBuilder().toJobParameters());
        jobExecution.setExecutionContext(jobExecutionContext);
        StepExecution stepExecution = new StepExecution(1, "deleteUnusedDeptStep", jobExecution);

        StepContext stepContext = mock(StepContext.class);
        lenient().when(stepContext.getStepExecution()).thenReturn(stepExecution);

        chunkContext = mock(ChunkContext.class);
        lenient().when(chunkContext.getStepContext()).thenReturn(stepContext);
    }

    private void setUsedDepts(Set<String> usedDepts) {
        jobExecutionContext.put("usedDepts", usedDepts);
    }

    // ---- cas nominal : suppression des départements non utilisés ----

    @Test
    void execute_shouldDeleteUnusedDepartments_fromDbAndLucene() throws Exception {
        setUsedDepts(Set.of("75", "69"));
        when(jdbc.queryForList(anyString(), eq(String.class))).thenReturn(List.of("75", "69", "33"));
        when(jdbc.queryForObject("select count(*) from ban", Integer.class)).thenReturn(1000, 950);

        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        ArgumentCaptor<MapSqlParameterSource> paramsCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(namedParameterJdbcTemplate).update(anyString(), paramsCaptor.capture());
        @SuppressWarnings("unchecked")
        Set<String> capturedDepts = (Set<String>) paramsCaptor.getValue().getValue("depts");
        assertThat(capturedDepts).containsExactly("33");

        verify(indexWriter).deleteDocuments(any(Query.class));
        verify(indexWriter).commit();
    }

    @Test
    void execute_shouldBuildLucenePrefixQuery_forEachRemainingDept() throws Exception {
        setUsedDepts(Set.of("75"));
        when(jdbc.queryForList(anyString(), eq(String.class))).thenReturn(List.of("75", "33", "44"));
        when(jdbc.queryForObject("select count(*) from ban", Integer.class)).thenReturn(100, 50);

        tasklet.execute(contribution, chunkContext);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(indexWriter).deleteDocuments(queryCaptor.capture());

        BooleanQuery query = (BooleanQuery) queryCaptor.getValue();

        List<String> prefixes = query.clauses().stream()
                .map(BooleanClause::getQuery)
                .map(PrefixQuery.class::cast)
                .map(prefixQuery -> prefixQuery.getPrefix().text())
                .toList();

        assertThat(prefixes).containsExactlyInAnyOrder("33", "44");
    }

    // ---- rien à supprimer ----

    @Test
    void execute_shouldNotDeleteAnything_whenAllDbDeptsAreUsed() throws Exception {
        setUsedDepts(Set.of("75", "69", "33"));
        when(jdbc.queryForList(anyString(), eq(String.class))).thenReturn(List.of("75", "69", "33"));

        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verifyNoInteractions(namedParameterJdbcTemplate);
        verifyNoInteractions(indexWriter);
        verify(jdbc, never()).queryForObject(anyString(), eq(Integer.class));
    }

    @Test
    void execute_shouldNotDeleteAnything_whenBdDeptsIsEmpty() throws Exception {
        setUsedDepts(null);
        when(jdbc.queryForList(anyString(), eq(String.class))).thenReturn(List.of());

        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verifyNoInteractions(namedParameterJdbcTemplate);
        verifyNoInteractions(indexWriter);
    }
}
