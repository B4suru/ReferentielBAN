package natsystem.tasklet;

import natsystem.shared.tasklet.AbstractRetrieveFileTasklet;
import natsystem.shared.tools.FileLocator;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractRetrieveFileTaskletTest {
    @Mock
    private StepContribution contribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    private ExecutionContext jobExecutionContext;
    private StepExecution stepExecution;

    private TestRetrieveFileTasklet tasklet;

    @BeforeEach
    void setUp() {
        jobExecutionContext = new ExecutionContext();

        JobInstance jobInstance = new JobInstance(1L, "testJob");

        JobExecution jobExecution = new JobExecution(
                1L,
                jobInstance,
                new JobParametersBuilder().toJobParameters()
        );

        jobExecution.setExecutionContext(jobExecutionContext);

        stepExecution = new StepExecution(
                1L,
                "retrieveFileStep",
                jobExecution
        );

        lenient()
                .when(chunkContext.getStepContext())
                .thenReturn(stepContext);

        lenient()
                .when(stepContext.getStepExecution())
                .thenReturn(stepExecution);

        tasklet = new TestRetrieveFileTasklet();

        org.springframework.test.util.ReflectionTestUtils.setField(
                tasklet,
                "dossierCsv",
                "/data/csv"
        );
    }


    // ---- Classe concrète utilisée uniquement pour les tests ----
    private static class TestRetrieveFileTasklet
            extends AbstractRetrieveFileTasklet {

        @Override
        protected String getExpectedHeader() {
            return "id;nom;prenom";
        }
    }


    // ---- Aucun fichier ----
    @Test
    void execute_shouldReturnFinished_whenNoFileFound() throws Exception {
        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {

            fileLocator
                    .when(() -> FileLocator.listFile("/data/csv"))
                    .thenReturn(List.of());

            RepeatStatus status = tasklet.execute(contribution, chunkContext);

            assertThat(status).isEqualTo(RepeatStatus.FINISHED);

            verify(contribution)
                    .setExitStatus(new ExitStatus("NO_INPUT_FILE"));

            assertThat(jobExecutionContext.getString("fileName"))
                    .isEmpty();

            assertThat(jobExecutionContext.getString("file"))
                    .isEmpty();

            assertThat(jobExecutionContext.containsKey("motifEchec"))
                    .isFalse();
        }
    }


    // ---- Plusieurs fichiers ----
    @Test
    void execute_shouldReturnFinished_whenMultipleFilesFound() throws Exception {
        Path file1 = Path.of("/tmp/file1.csv");
        Path file2 = Path.of("/tmp/file2.csv");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {

            fileLocator
                    .when(() -> FileLocator.listFile("/data/csv"))
                    .thenReturn(List.of(file1, file2));

            RepeatStatus status = tasklet.execute(contribution, chunkContext);

            assertThat(status).isEqualTo(RepeatStatus.FINISHED);

            verify(contribution)
                    .setExitStatus(new ExitStatus("MULTIPLE_FILES_FOUND"));

            assertThat(jobExecutionContext.getString("motifEchec"))
                    .isEqualTo("MULTIPLE_FILES_FOUND");

            assertThat(jobExecutionContext.getString("fileName"))
                    .isEmpty();

            assertThat(jobExecutionContext.getString("file"))
                    .isEmpty();
        }
    }

    // ---- Fichier valide ----
    @Test
    void execute_shouldStoreFileInformation_whenOneValidFileIsFound()
            throws Exception {

        Path filePath = Path.of("/tmp/input.csv");

        try (
                MockedStatic<FileLocator> fileLocator =
                        mockStatic(FileLocator.class);

                MockedConstruction<FileManager> fileManagerConstruction =
                        mockConstruction(
                                FileManager.class,
                                (mock, context) -> {
                                    when(mock.isCsvValid("id;nom;prenom"))
                                            .thenReturn(true);
                                }
                        )
        ) {

            fileLocator
                    .when(() -> FileLocator.listFile("/data/csv"))
                    .thenReturn(List.of(filePath));

            RepeatStatus status = tasklet.execute(contribution, chunkContext);

            // Ton code actuel retourne null ici.
            assertThat(status).isNull();

            assertThat(jobExecutionContext.getString("fileName"))
                    .isEqualTo("input.csv");

            assertThat(jobExecutionContext.getString("file"))
                    .isEqualTo(filePath.toString());

            FileManager fileManager =
                    fileManagerConstruction.constructed().getFirst();

            verify(fileManager)
                    .setFile(filePath.toString());

            verify(fileManager)
                    .isCsvValid("id;nom;prenom");

            verify(fileManager)
                    .sortCSV();

            verify(contribution, never())
                    .setExitStatus(any());
        }
    }

    // ---- CSV invalide ----
    @Test
    void execute_shouldReturnCsvNotValid_whenCsvIsInvalid()
            throws Exception {

        Path filePath = Path.of("/tmp/input.csv");

        try (
                MockedStatic<FileLocator> fileLocator =
                        mockStatic(FileLocator.class);

                MockedConstruction<FileManager> fileManagerConstruction =
                        mockConstruction(
                                FileManager.class,
                                (mock, context) -> {
                                    when(mock.isCsvValid("id;nom;prenom"))
                                            .thenReturn(false);
                                }
                        )
        ) {

            fileLocator
                    .when(() -> FileLocator.listFile("/data/csv"))
                    .thenReturn(List.of(filePath));

            RepeatStatus status = tasklet.execute(contribution, chunkContext);

            assertThat(status).isEqualTo(RepeatStatus.FINISHED);

            verify(contribution)
                    .setExitStatus(new ExitStatus("CSV_NOT_VALID"));

            assertThat(jobExecutionContext.getString("motifEchec"))
                    .isEqualTo("CSV_NOT_VALID");

            assertThat(jobExecutionContext.getString("fileName"))
                    .isEqualTo("input.csv");

            assertThat(jobExecutionContext.getString("file"))
                    .isEqualTo(filePath.toString());

            FileManager fileManager =
                    fileManagerConstruction.constructed().getFirst();

            verify(fileManager)
                    .setFile(filePath.toString());

            verify(fileManager)
                    .isCsvValid("id;nom;prenom");

            verify(fileManager, never())
                    .sortCSV();
        }
    }
}
