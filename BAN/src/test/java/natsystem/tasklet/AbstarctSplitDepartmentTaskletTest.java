package natsystem.tasklet;

import natsystem.shared.tasklet.AbstractSplitDepartmentTasklet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbstarctSplitDepartmentTaskletTest {
    @TempDir
    Path tempDir;

    private final Path outputDir = Path.of("csv");

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(outputDir)) {
            try (var paths = Files.walk(outputDir)) {
                paths.sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }

    @Test
    void shouldSplitFileByDepartment() throws Exception {
        Path inputFile = createInputFile(
                "id;name;department",
                "1;Alice;FINANCE",
                "2;Bob;FINANCE",
                "3;Charlie;HR",
                "4;David;HR",
                "5;Eve;IT"
        );

        TestTasklet tasklet = new TestTasklet(inputFile);

        StepExecution stepExecution = createStepExecution();
        StepContribution contribution = new StepContribution(stepExecution);
        ChunkContext chunkContext = new ChunkContext(
                new StepContext(stepExecution)
        );

        RepeatStatus result = tasklet.execute(
                contribution,
                chunkContext
        );

        assertEquals(RepeatStatus.FINISHED, result);

        assertEquals(
                List.of(
                        "id;name;department",
                        "1;Alice;FINANCE",
                        "2;Bob;FINANCE"
                ),
                Files.readAllLines(outputDir.resolve("FINANCE.csv"))
        );

        assertEquals(
                List.of(
                        "id;name;department",
                        "3;Charlie;HR",
                        "4;David;HR"
                ),
                Files.readAllLines(outputDir.resolve("HR.csv"))
        );

        assertEquals(
                List.of(
                        "id;name;department",
                        "5;Eve;IT"
                ),
                Files.readAllLines(outputDir.resolve("IT.csv"))
        );
    }

    @Test
    void shouldCreateOnlyOneFileWhenThereIsOneDepartment()
            throws Exception {

        Path inputFile = createInputFile(
                "id;name;department",
                "1;Alice;IT",
                "2;Bob;IT",
                "3;Charlie;IT"
        );

        TestTasklet tasklet = new TestTasklet(inputFile);

        StepExecution stepExecution = createStepExecution();

        tasklet.execute(
                new StepContribution(stepExecution),
                new ChunkContext(new StepContext(stepExecution))
        );

        assertTrue(Files.exists(outputDir.resolve("IT.csv")));

        assertEquals(
                List.of(
                        "id;name;department",
                        "1;Alice;IT",
                        "2;Bob;IT",
                        "3;Charlie;IT"
                ),
                Files.readAllLines(outputDir.resolve("IT.csv"))
        );

        try (var files = Files.list(outputDir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void shouldKeepHeaderInEveryDepartmentFile() throws Exception {
        Path inputFile = createInputFile(
                "id;name;department",
                "1;Alice;HR",
                "2;Bob;IT"
        );

        TestTasklet tasklet = new TestTasklet(inputFile);

        StepExecution stepExecution = createStepExecution();

        tasklet.execute(
                new StepContribution(stepExecution),
                new ChunkContext(new StepContext(stepExecution))
        );

        assertEquals(
                "id;name;department",
                Files.readAllLines(outputDir.resolve("HR.csv")).getFirst()
        );

        assertEquals(
                "id;name;department",
                Files.readAllLines(outputDir.resolve("IT.csv")).getFirst()
        );
    }

    @Test
    void shouldDoNothingWhenFileContainsOnlyHeader()
            throws Exception {

        Path inputFile = createInputFile(
                "id;name;department"
        );

        TestTasklet tasklet = new TestTasklet(inputFile);

        StepExecution stepExecution = createStepExecution();

        RepeatStatus result = tasklet.execute(
                new StepContribution(stepExecution),
                new ChunkContext(new StepContext(stepExecution))
        );

        assertEquals(RepeatStatus.FINISHED, result);
        assertTrue(Files.exists(outputDir));

        try (var files = Files.list(outputDir)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void shouldDoNothingWhenFileIsEmpty() throws Exception {
        Path inputFile = createInputFile();

        TestTasklet tasklet = new TestTasklet(inputFile);

        StepExecution stepExecution = createStepExecution();

        RepeatStatus result = tasklet.execute(
                new StepContribution(stepExecution),
                new ChunkContext(new StepContext(stepExecution))
        );

        assertEquals(RepeatStatus.FINISHED, result);
        assertTrue(Files.exists(outputDir));

        try (var files = Files.list(outputDir)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void shouldCallAfterExecution() throws Exception {
        Path inputFile = createInputFile(
                "id;name;department",
                "1;Alice;IT"
        );

        TestTasklet tasklet = new TestTasklet(inputFile);

        StepExecution stepExecution = createStepExecution();

        tasklet.execute(
                new StepContribution(stepExecution),
                new ChunkContext(new StepContext(stepExecution))
        );

        assertTrue(tasklet.afterExecutionCalled);
    }

    private StepExecution createStepExecution() {
        JobInstance jobInstance = new JobInstance(1L, "testJob");

        JobExecution jobExecution = new JobExecution(
                1L,
                jobInstance,
                new JobParametersBuilder().toJobParameters()
        );
        return new StepExecution(1,
                "splitDepartmentStep",
                jobExecution
        );
    }

    private Path createInputFile(String... lines) throws IOException {
        Path inputFile = tempDir.resolve("input.csv");

        Files.write(inputFile, List.of(lines));

        return inputFile;
    }

    private static class TestTasklet
            extends AbstractSplitDepartmentTasklet {

        private final Path inputFile;

        private boolean afterExecutionCalled;

        private TestTasklet(Path inputFile) {
            this.inputFile = inputFile;
        }

        @Override
        protected String getInputFile(ChunkContext chunkContext) {
            return inputFile.toString();
        }

        @Override
        protected String extractDepartment(String line) {
            return line.substring(
                    line.lastIndexOf(';') + 1
            );
        }

        @Override
        protected void afterExecution(ChunkContext chunkContext) {
            afterExecutionCalled = true;
        }
    }

}
