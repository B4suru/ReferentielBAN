package natsystem.listener;

import natsystem.shared.listener.MasterStepListener;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MasterStepListenerTest {

    @Mock
    private FileManager rapport;

    private JobExecution jobExecution;
    private MasterStepListener listener;

    @BeforeEach
    void setUp() {
        listener = new MasterStepListener(rapport);

        JobInstance jobInstance = new JobInstance(1L, "testJob");
        jobExecution = new JobExecution(1L, jobInstance, new JobParametersBuilder().toJobParameters());
    }

    private StepExecution createStep(String stepName) {
        StepExecution step = new StepExecution(1, stepName, jobExecution);
        jobExecution.addStepExecutions(List.of(step));
        return step;
    }

    private StepExecution addPartition(String stepName, long inserted, long updated, long deleted) {
        StepExecution step = createStep(stepName);
        step.getExecutionContext().putLong("nbInserted", inserted);
        step.getExecutionContext().putLong("nbUpdated", updated);
        step.getExecutionContext().putLong("nbDeleted", deleted);
        return step;
    }

    @Test
    void afterStep_shouldAggregateCounts_acrossMatchingPartitions() {
        addPartition("banBatchStep:0", 100, 20, 5);
        addPartition("banBatchStep:1", 50, 10, 0);

        StepExecution masterStep = createStep("banMasterStep");
        masterStep.setReadCount(1000);
        masterStep.setWriteCount(950);
        masterStep.setFilterCount(50);
        masterStep.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(masterStep);

        verify(rapport).write("Insertions             : 150");
        verify(rapport).write("Mises à jour           : 30");
        verify(rapport).write("Suppressions           : 5");
        verify(rapport).write("Lignes total lues      : 1000");
        verify(rapport).write("Lignes total écrites   : 950");
        verify(rapport).write("Lignes total filtrées  : 50");
    }

    @Test
    void afterStep_shouldIgnoreStepsNotMatchingBanBatchStepPrefix() {
        addPartition("banBatchStep:0", 100, 20, 5);
        addPartition("autreJobStep:0", 9999, 9999, 9999); // ne doit pas être compté
        StepExecution masterStep = createStep("banMasterStep");
        masterStep.setReadCount(0);
        masterStep.setWriteCount(0);
        masterStep.setFilterCount(0);
        masterStep.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(masterStep);

        verify(rapport).write("Insertions             : 100");
        verify(rapport).write("Mises à jour           : 20");
        verify(rapport).write("Suppressions           : 5");
    }

    @Test
    void afterStep_shouldDefaultMissingCounts_toZero() {
        // partition sans nbInserted/nbUpdated/nbDeleted dans son ExecutionContext

        createStep("banBatchStep:0");
        StepExecution masterStep = createStep("banMasterStep");
        masterStep.setReadCount(0);
        masterStep.setWriteCount(0);
        masterStep.setFilterCount(0);
        masterStep.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(masterStep);

        verify(rapport).write("Insertions             : 0");
        verify(rapport).write("Mises à jour           : 0");
        verify(rapport).write("Suppressions           : 0");
    }

    @Test
    void afterStep_shouldReturnTheStepExecutionExitStatus_unchanged() {
        StepExecution masterStep = createStep("banMasterStep");
        ExitStatus failedStatus = new ExitStatus("FAILED", "erreur de test");
        masterStep.setExitStatus(failedStatus);

        ExitStatus result = listener.afterStep(masterStep);

        assertThat(result).isEqualTo(failedStatus);
    }

    @Test
    void afterStep_shouldNotCountPartitions_whenNoneMatchPrefix() {
        addPartition("autreStep:0", 100, 20, 5);

        StepExecution masterStep = createStep("banMasterStep");
        masterStep.setExitStatus(ExitStatus.COMPLETED);

        listener.afterStep(masterStep);

        verify(rapport).write("Insertions             : 0");
        verify(rapport).write("Mises à jour           : 0");
        verify(rapport).write("Suppressions           : 0");
        verify(rapport, never()).write("Insertions             : 100");
    }
}
