package natsystem.listener;

import natsystem.dvf.batch.config.listener.DvfStepListener;
import natsystem.shared.tools.FileManager;
import natsystem.shared.tools.Tool;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DvfStepListenerTest {
    @Mock
    private FileManager rapport;

    private JobExecution jobExecution;
    private DvfStepListener listener;
    private MockedStatic<Tool> toolMock;

    @BeforeEach
    void setUp() {
        JobInstance jobInstance = new JobInstance(1L, "testJob");
        jobExecution = new JobExecution(1L, jobInstance, new JobParametersBuilder().toJobParameters());
        listener = new DvfStepListener(rapport);
        toolMock = mockStatic(Tool.class);
    }

    @AfterEach
    void tearDown() {
        toolMock.close();
    }

    private StepExecution buildStepExecution(String stepName) {
        StepExecution step = new StepExecution(1, stepName, jobExecution);
        step.setExitStatus(ExitStatus.COMPLETED);
        return step;
    }

    @Test
    void afterReport_shouldDeleteTempFile_usingFileFromExecutionContext() {
        StepExecution step = buildStepExecution("dvfBatchStep:0");
        step.getExecutionContext().putString("file", "/fake/dvf/data.csv");

        listener.afterStep(step);

        toolMock.verify(() -> Tool.deleteTempFile("/fake/dvf/data.csv"));
    }


    @Test
    void afterStep_shouldReturnStepExecutionExitStatus_unchanged() {
        StepExecution step = buildStepExecution("dvfBatchStep:0");
        step.getExecutionContext().putString("file", "/fake/dvf/data.csv");
        ExitStatus failed = new ExitStatus("FAILED", "erreur de test");
        step.setExitStatus(failed);

        ExitStatus result = listener.afterStep(step);

        assertThat(result).isEqualTo(failed);
    }

    @Test
    void afterStep_shouldWriteOnlyCommonReportLines_noCustomLines() {
        StepExecution step = buildStepExecution("dvfBatchStep:0");
        step.getExecutionContext().putString("file", "/fake/dvf/data.csv");
        step.setReadCount(200);
        step.setWriteCount(190);
        step.setFilterCount(10);

        listener.afterStep(step);

        verify(rapport).write("-------- dvfBatchStep:0 --------");
        verify(rapport).write("Lignes lues      : 200");
        verify(rapport).write("Lignes écrites   : 190");
        verify(rapport).write("Lignes filtrées  : 10");
    }
}
