package natsystem.listener;

import natsystem.shared.listener.JobListener;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobListenerTest {

    @Mock
    private FileManager rapport;

    @Mock
    private FileManager logs;

    private JobListener listener;

    @BeforeEach
    void setUp() {
        listener = new JobListener(rapport, logs);
    }

    private JobExecution buildJobExecution(JobParameters params) {
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(1L, jobInstance, params);
        jobExecution.setExecutionContext(new ExecutionContext());
        return jobExecution;
    }

    // ---- beforeJob ----

    @Test
    void beforeJob_shouldOpenBothFileManagers_withNamesFromJobParameters() {
        JobParameters params = new JobParametersBuilder()
                .addString("rapportFileName", "Rapport/Rapport_test.txt", false)
                .addString("logFileName", "Logs/Logs_test.txt", false)
                .toJobParameters();
        JobExecution jobExecution = buildJobExecution(params);

        listener.beforeJob(jobExecution);

        verify(rapport).open("Rapport/Rapport_test.txt");
        verify(logs).open("Logs/Logs_test.txt");
    }

    @Test
    void beforeJob_shouldWriteDateLine_toRapport() {
        JobParameters params = new JobParametersBuilder()
                .addString("rapportFileName", "r.txt", false)
                .addString("logFileName", "l.txt", false)
                .toJobParameters();
        JobExecution jobExecution = buildJobExecution(params);

        listener.beforeJob(jobExecution);

        verify(rapport).write(startsWith("Date : "));
    }

    @Test
    void beforeJob_shouldWriteChecksumLine_whenPresent() {
        JobParameters params = new JobParametersBuilder()
                .addString("rapportFileName", "r.txt", false)
                .addString("logFileName", "l.txt", false)
                .addString("checksum", "abc123", false)
                .toJobParameters();
        JobExecution jobExecution = buildJobExecution(params);

        listener.beforeJob(jobExecution);

        verify(rapport).write("Checksum du fichier : abc123");
    }

    @Test
    void beforeJob_shouldNotWriteChecksumLine_whenAbsent() {
        JobParameters params = new JobParametersBuilder()
                .addString("rapportFileName", "r.txt", false)
                .addString("logFileName", "l.txt", false)
                .toJobParameters();
        JobExecution jobExecution = buildJobExecution(params);

        listener.beforeJob(jobExecution);

        verify(rapport, never()).write(contains("Checksum"));
    }

    // ---- afterJob : archivage ----

    @Test
    void afterJob_shouldArchiveFile_whenPathIsNotEmpty() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "/fake/csv/folder/data.csv");
        jobExecution.getExecutionContext().putString("fileName", "data.csv");
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStatus(BatchStatus.COMPLETED);

        try (MockedConstruction<FileManager> fileManagerMock = mockConstruction(FileManager.class)) {
            listener.afterJob(jobExecution);

            FileManager archiver = fileManagerMock.constructed().get(0);
            verify(archiver).setFile("/fake/csv/folder/data.csv");
            verify(archiver).archiverFichier("data.csv");
        }
    }

    @Test
    void afterJob_shouldNotArchive_whenPathIsEmpty() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStatus(BatchStatus.COMPLETED);

        try (MockedConstruction<FileManager> fileManagerMock = mockConstruction(FileManager.class)) {
            listener.afterJob(jobExecution);

            assertThat(fileManagerMock.constructed()).isEmpty();
        }
    }

    // ---- afterJob : statut / exitStatus ----

    @Test
    void afterJob_shouldWriteStatus_andExitCode_onSuccess() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);

        listener.afterJob(jobExecution);

        verify(rapport).write(contains("Statut du job"));
        verify(rapport).write("ExitStatus du job      : COMPLETED");
    }

    @Test
    void afterJob_shouldWriteMotifEchec_insteadOfExitCode_whenFailedWithMotif() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.getExecutionContext().putString("motifEchec", "CSV_NOT_VALID");
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(new ExitStatus("FAILED"));

        listener.afterJob(jobExecution);

        verify(rapport).write("ExitStatus du job      : CSV_NOT_VALID");
        verify(rapport, never()).write("ExitStatus du job      : FAILED");
    }

    @Test
    void afterJob_shouldWriteExitCode_whenFailedButNoMotif() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        // pas de motifEchec renseigné
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(new ExitStatus("FAILED"));

        listener.afterJob(jobExecution);

        verify(rapport).write("ExitStatus du job      : FAILED");
    }

    @Test
    void afterJob_shouldWriteNoInputFileMessage_whenExitCodeIsNoInputFile() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(new ExitStatus("NO_INPUT_FILE"));

        listener.afterJob(jobExecution);

        verify(rapport).write("Aucun fichier à traiter");
    }

    @Test
    void afterJob_shouldWriteDuration_whenStartAndEndTimeArePresent() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0));
        jobExecution.setEndTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 2, 30));

        listener.afterJob(jobExecution);

        verify(rapport).write("Durée traitement       : 2min 150s");
    }

    @Test
    void afterJob_shouldNotWriteDuration_whenStartTimeIsMissing() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setEndTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 2, 30));
        // startTime jamais renseigné

        listener.afterJob(jobExecution);

        verify(rapport, never()).write(contains("Durée traitement"));
    }

    @Test
    void afterJob_shouldNotWriteDuration_whenEndTimeIsMissing() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0));
        // startTime jamais renseigné

        listener.afterJob(jobExecution);

        verify(rapport, never()).write(contains("Durée traitement"));
    }

    // ---- afterJob : fermeture ----

    @Test
    void afterJob_shouldCloseBothFileManagers() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);

        listener.afterJob(jobExecution);

        verify(rapport).close();
        verify(logs).close();
    }

    @Test
    void afterJob_shouldLogAbsolutePaths_ofBothFiles() {
        JobExecution jobExecution = buildJobExecution(new JobParametersBuilder().toJobParameters());
        jobExecution.getExecutionContext().putString("file", "");
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        when(logs.getAbsolutePath()).thenReturn("/fake/Logs/Logs_test.txt");
        when(rapport.getAbsolutePath()).thenReturn("/fake/Rapport/Rapport_test.txt");

        listener.afterJob(jobExecution);

        verify(logs).getAbsolutePath();
        verify(rapport).getAbsolutePath();
    }
}
