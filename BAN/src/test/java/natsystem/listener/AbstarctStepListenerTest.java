package natsystem.listener;

import natsystem.shared.listener.AbstractStepListener;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstarctStepListenerTest {
    @Mock
    private FileManager rapport;

    private JobExecution jobExecution;

    @BeforeEach
    void setUp() {
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        jobExecution = new JobExecution(1L, jobInstance, new JobParametersBuilder().toJobParameters());
    }

    /** Sous-classe minimale, aucun hook surchargé -> comportement par défaut du template. */
    private static class NoOpStepListener extends AbstractStepListener {
        NoOpStepListener(FileManager rapport) {
            super(rapport);
        }
    }

    /** Sous-classe qui matérialise chaque hook par une ligne écrite, pour vérifier l'ordre. */
    private static class RecordingStepListener extends AbstractStepListener {
        RecordingStepListener(FileManager rapport) {
            super(rapport);
        }

        @Override
        protected void beforeReport(StepExecution stepExecution) {
            writeLine("BEFORE");
        }

        @Override
        protected void writeCustomReport(StepExecution stepExecution) {
            writeLine("CUSTOM");
        }

        @Override
        protected void afterReport(StepExecution stepExecution) {
            writeLine("AFTER");
        }
    }

    private StepExecution buildStepExecution(String stepName) {
        return new StepExecution(1, stepName, jobExecution);
    }

    // ---- rapport commun ----

    @Test
    void afterStep_shouldWriteCommonReportLines() {
        StepExecution step = buildStepExecution("monStep");
        step.setReadCount(100);
        step.setWriteCount(95);
        step.setFilterCount(5);
        step.setExitStatus(ExitStatus.COMPLETED);

        new NoOpStepListener(rapport).afterStep(step);

        verify(rapport).write("-------- monStep --------");
        verify(rapport).write("Lignes lues      : 100");
        verify(rapport).write("Lignes écrites   : 95");
        verify(rapport).write("Lignes filtrées  : 5");
    }

    // ---- hooks par défaut (no-op) ----

    @Test
    void afterStep_shouldWriteOnlyCommonLines_whenNoHookIsOverridden() {
        StepExecution step = buildStepExecution("monStep");
        step.setExitStatus(ExitStatus.COMPLETED);
        // pas de startTime/endTime -> pas de ligne de durée non plus

        new NoOpStepListener(rapport).afterStep(step);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rapport, times(4)).write(captor.capture());
        assertThat(captor.getAllValues()).containsExactly(
                "-------- monStep --------",
                "Lignes lues      : 0",
                "Lignes écrites   : 0",
                "Lignes filtrées  : 0"
        );
    }

    // ---- ordre des hooks ----

    @Test
    void afterStep_shouldCallHooks_inTemplateOrder() {
        StepExecution step = buildStepExecution("monStep");
        step.setReadCount(10);
        step.setWriteCount(9);
        step.setFilterCount(1);
        step.setExitStatus(ExitStatus.COMPLETED);
        step.setStartTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0));
        step.setEndTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 1, 0));

        new RecordingStepListener(rapport).afterStep(step);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rapport, times(8)).write(captor.capture());
        List<String> lines = captor.getAllValues();

        // BEFORE (beforeReport) -> rapport commun -> CUSTOM (writeCustomReport)
        // -> durée -> AFTER (afterReport), dans cet ordre précis
        assertThat(lines).containsExactly(
                "BEFORE",
                "-------- monStep --------",
                "Lignes lues      : 10",
                "Lignes écrites   : 9",
                "Lignes filtrées  : 1",
                "CUSTOM",
                "Temps step       : 1min 60s 60000ms",
                "AFTER"
        );
    }

    // ---- durée ----

    @Test
    void afterStep_shouldWriteDuration_whenStartAndEndTimePresent() {
        StepExecution step = buildStepExecution("monStep");
        step.setExitStatus(ExitStatus.COMPLETED);
        step.setStartTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0));
        step.setEndTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 2, 30));

        new NoOpStepListener(rapport).afterStep(step);

        verify(rapport).write("Temps step       : 2min 150s 150000ms");
    }

    @Test
    void afterStep_shouldNotWriteDuration_whenStartTimeIsMissing() {
        StepExecution step = buildStepExecution("monStep");
        step.setExitStatus(ExitStatus.COMPLETED);
        step.setEndTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 2, 30));

        new NoOpStepListener(rapport).afterStep(step);

        verify(rapport, never()).write(contains("Temps step"));
    }

    @Test
    void afterStep_shouldNotWriteDuration_whenEndTimeIsMissing() {
        StepExecution step = buildStepExecution("monStep");
        step.setExitStatus(ExitStatus.COMPLETED);
        step.setStartTime(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0, 0));

        new NoOpStepListener(rapport).afterStep(step);

        verify(rapport, never()).write(contains("Temps step"));
    }

    // ---- valeur de retour ----

    @Test
    void afterStep_shouldReturnStepExecutionExitStatus_unchanged() {
        StepExecution step = buildStepExecution("monStep");
        ExitStatus failed = new ExitStatus("FAILED", "erreur de test");
        step.setExitStatus(failed);

        ExitStatus result = new NoOpStepListener(rapport).afterStep(step);

        assertThat(result).isEqualTo(failed);
    }

    // ---- writeLine (utilitaire protégé) ----

    @Test
    void writeLine_shouldDelegateDirectlyToRapportWrite() {
        StepExecution step = buildStepExecution("monStep");
        step.setExitStatus(ExitStatus.COMPLETED);

        new RecordingStepListener(rapport).afterStep(step);

        verify(rapport).write("CUSTOM");
    }
}
