package natsystem.initialization;

import natsystem.BatchInitialization;
import natsystem.shared.tools.FileLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchInitializationTest {
    private static final String FOLDER = "/fake/csv/folder";

    @Mock
    private JobOperator launcher;

    @Mock
    private JobExecution jobExecution;

    private Map<String, Job> jobs;
    private BatchInitialization batchInitialization;

    @BeforeEach
    void setUp() throws Exception {
        jobs = new HashMap<>();
        batchInitialization = new BatchInitialization(launcher, jobs);
        ReflectionTestUtils.setField(batchInitialization, "folder", FOLDER);
        ReflectionTestUtils.setField(batchInitialization, "isRecuperationActif", false);

        lenient().when(launcher.start(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);
    }

    @AfterEach
    void cleanUpCreatedDirectories() throws IOException {
        deleteIfEmpty(Path.of("Logs"));
        deleteIfEmpty(Path.of("Rapport"));
    }

    private void deleteIfEmpty(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            if (!stream.iterator().hasNext()) {
                Files.delete(dir);
            }
        }
    }

    private Job registerJob(String name) {
        Job job = mock(Job.class);
        jobs.put(name, job);
        return job;
    }


    // ---- cas nominal ----

    @Test
    void executerBatch_shouldStartJob_andReturnItsJobExecution() throws Exception {
        Job job = registerJob("jobImportBan");
        Path fakeFile = Path.of("/fake/csv/folder/adresses-79.csv");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of(fakeFile));
            fileLocator.when(() -> FileLocator.sha256(fakeFile)).thenReturn("hash123");

            JobExecution result = batchInitialization.executerBatch("jobImportBan");

            assertThat(result).isSameAs(jobExecution);
            verify(launcher).start(eq(job), any(JobParameters.class));
        }
    }

    @Test
    void executerBatch_shouldIncludeChecksum_whenFileFound() throws Exception {
        Job job = registerJob("jobImportBan");
        Path fakeFile = Path.of("/fake/csv/folder/adresses-79.csv");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of(fakeFile));
            fileLocator.when(() -> FileLocator.sha256(fakeFile)).thenReturn("hash123");

            batchInitialization.executerBatch("jobImportBan");

            ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
            verify(launcher).start(eq(job), captor.capture());
            assertThat(captor.getValue().getString("checksum")).isEqualTo("hash123");
        }
    }


    @Test
    void executerBatch_shouldSetStartAtParameter_toCurrentTime() throws Exception {
        Job job = registerJob("jobImportBan");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of());

            long before = System.currentTimeMillis();
            batchInitialization.executerBatch("jobImportBan");
            long after = System.currentTimeMillis();

            ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
            verify(launcher).start(eq(job), captor.capture());
            Long startAt = captor.getValue().getLong("startAt");

            assertThat(startAt).isNotNull();
            assertThat(startAt).isBetween(before, after);
        }
    }

    @Test
    void executerBatch_shouldSetLogAndRapportFileNames_withExpectedPattern() throws Exception {
        Job job = registerJob("jobImportBan");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of());

            batchInitialization.executerBatch("jobImportBan");

            ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
            verify(launcher).start(eq(job), captor.capture());
            JobParameters params = captor.getValue();

            assertThat(params.getString("logFileName"))
                    .matches("Logs/Logs_jobImportBan_\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\\.txt");
            assertThat(params.getString("rapportFileName"))
                    .matches("Rapport/Rapport_jobImportBan_\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}\\.txt");
        }
    }


    // ---- téléchargement automatique quand aucun fichier n'est présent ----

    @Test
    void executerBatch_shouldDownloadBanFile_whenNoFileFoundAndRecuperationActive() throws Exception {
        Job job = registerJob("jobImportBan");
        ReflectionTestUtils.setField(batchInitialization, "isRecuperationActif", true);
        Path downloadedFile = Path.of("/fake/csv/folder/adresses-79.csv");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER))
                    .thenReturn(List.of(), List.of(downloadedFile));
            fileLocator.when(() -> FileLocator.sha256(downloadedFile)).thenReturn("hash-ban");

            batchInitialization.executerBatch("jobImportBan");

            fileLocator.verify(() -> FileLocator.downloadFile(
                    "https://adresse.data.gouv.fr/data/ban/adresses/2026-06-17/csv/adresses-79.csv.gz",
                    FOLDER, "adresses-79.csv"));
            verify(launcher).start(eq(job), any(JobParameters.class));
        }
    }

    @Test
    void executerBatch_shouldDownloadDvfFile_whenNoFileFoundAndRecuperationActive() throws Exception {
        registerJob("jobImportDvf");
        ReflectionTestUtils.setField(batchInitialization, "isRecuperationActif", true);

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of());

            batchInitialization.executerBatch("jobImportDvf");

            fileLocator.verify(() -> FileLocator.downloadFile(
                    "https://files.data.gouv.fr/geo-dvf/latest/csv/2025/departements/79.csv.gz",
                    FOLDER, "dvf-79.csv"));
        }
    }

    @Test
    void executerBatch_shouldDownloadGeoJsonFile_whenNoFileFoundAndRecuperationActive() throws Exception {
        registerJob("jobImportGeoJSON");
        ReflectionTestUtils.setField(batchInitialization, "isRecuperationActif", true);

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of());

            batchInitialization.executerBatch("jobImportGeoJSON");

            fileLocator.verify(() -> FileLocator.downloadFile(
                    "https://adresse.data.gouv.fr/data/contours-administratifs/2023/geojson/communes-100m.geojson.gz",
                    FOLDER, "communes.geojson"));
        }
    }

    @Test
    void executerBatch_shouldNotDownload_whenFileAlreadyPresent() throws Exception {
        Job job = registerJob("jobImportBan");
        ReflectionTestUtils.setField(batchInitialization, "isRecuperationActif", true);
        Path existingFile = Path.of("/fake/csv/folder/adresses-79.csv");

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of(existingFile));
            fileLocator.when(() -> FileLocator.sha256(existingFile)).thenReturn("hash-existant");

            batchInitialization.executerBatch("jobImportBan");

            fileLocator.verify(() -> FileLocator.downloadFile(anyString(), anyString(), anyString()), never());
            verify(launcher).start(eq(job), any(JobParameters.class));
        }
    }

    @Test
    void executerBatch_shouldNotDownload_whenRecuperationIsInactive_evenIfNoFileFound() throws Exception {
        Job job = registerJob("jobImportBan");
        // isRecuperationActif reste à false (valeur par défaut posée dans setUp())

        try (MockedStatic<FileLocator> fileLocator = mockStatic(FileLocator.class)) {
            fileLocator.when(() -> FileLocator.listFile(FOLDER)).thenReturn(List.of());

            batchInitialization.executerBatch("jobImportBan");

            fileLocator.verify(() -> FileLocator.downloadFile(anyString(), anyString(), anyString()), never());
            verify(launcher).start(eq(job), any(JobParameters.class));
        }
    }
}
