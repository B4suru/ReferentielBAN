package natsystem.tasklet;
import natsystem.ban.batch.config.tasklet.SplitBanDepartmentTasklet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepExecution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitBanDepartmentTaskletTest {
    private SplitBanDepartmentTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new SplitBanDepartmentTasklet();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of("csv_sorted.csv"));
    }

    @Test
    void shouldReturnInputFile() {
        ChunkContext chunkContext = createChunkContext();

        String result = tasklet.getInputFile(chunkContext);

        assertEquals("csv_sorted.csv", result);
    }

    @ParameterizedTest
    @CsvSource({
            "750001;some data, 75",
            "010001;some data, 01",
            "971001;some data, 971",
            "981001;some data, 981",
    })
    void shouldExtractDepartment(String line, String expectedDepartment) {
        String result = tasklet.extractDepartment(line);
        assertEquals(expectedDepartment, result);
    }

    @Test
    void shouldAddExtractedDepartmentToUsedDepartments() {
        tasklet.extractDepartment("750001;some data");
        tasklet.extractDepartment("690001;some data");
        tasklet.extractDepartment("971001;some data");

        ChunkContext chunkContext = createChunkContext();

        tasklet.afterExecution(chunkContext);

        Object value = chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("usedDepts");

        assertTrue(value instanceof Set<?>);

        @SuppressWarnings("unchecked")
        Set<String> usedDepts = (Set<String>) value;

        assertEquals(
                Set.of("75", "69", "971"),
                usedDepts
        );
    }

    @Test
    void shouldNotDuplicateDepartment() {
        tasklet.extractDepartment("750001;some data");
        tasklet.extractDepartment("750002;some data");
        tasklet.extractDepartment("750003;some data");

        ChunkContext chunkContext = createChunkContext();

        tasklet.afterExecution(chunkContext);

        @SuppressWarnings("unchecked")
        Set<String> usedDepts = (Set<String>) chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("usedDepts");

        assertEquals(Set.of("75"), usedDepts);
    }

    @Test
    void shouldDeleteTemporaryFileAfterExecution() throws Exception {
        Path tempFile = Path.of("csv_sorted.csv");
        Files.writeString(tempFile, "750001;some data");

        assertTrue(Files.exists(tempFile));

        ChunkContext chunkContext = createChunkContext();

        tasklet.afterExecution(chunkContext);

        assertTrue(Files.notExists(tempFile));
    }

    @Test
    void shouldPutUsedDepartmentsInJobExecutionContext() {
        tasklet.extractDepartment("750001;some data");
        tasklet.extractDepartment("690001;some data");
        tasklet.extractDepartment("971001;some data");
        tasklet.extractDepartment("981001;some data");

        ChunkContext chunkContext = createChunkContext();

        tasklet.afterExecution(chunkContext);

        @SuppressWarnings("unchecked")
        Set<String> usedDepts = (Set<String>) chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("usedDepts");

        assertEquals(
                Set.of("75", "69", "971", "981"),
                usedDepts
        );
    }

    private ChunkContext createChunkContext() {
        JobInstance jobInstance = new JobInstance(1L, "testJob");

        JobExecution jobExecution = new JobExecution(
                1L,
                jobInstance,
                new JobParametersBuilder().toJobParameters()
        );


        StepExecution stepExecution = new StepExecution(
                1,
                "splitBanDepartmentStep",
                jobExecution
        );

        return new ChunkContext(
                new StepContext(stepExecution)
        );
    }
}
