package natsystem.shared.listener;

import natsystem.shared.tools.FileManager;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class AbstractStepListener implements StepExecutionListener {

    private final FileManager rapport;

    protected AbstractStepListener(FileManager rapport) {
        this.rapport = rapport;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        beforeReport(stepExecution);
        writeCommonReport(stepExecution);
        writeCustomReport(stepExecution);
        writeDuration(stepExecution);
        afterReport(stepExecution);
        return stepExecution.getExitStatus();
    }

    protected void beforeReport(StepExecution stepExecution) {
    }

    protected void afterReport(StepExecution stepExecution) {
    }

    protected void writeCustomReport(StepExecution stepExecution) {
    }

    private void writeCommonReport(StepExecution stepExecution) {

        rapport.write("-------- " + stepExecution.getStepName() + " --------");
        rapport.write("Lignes lues      : " + stepExecution.getReadCount());
        rapport.write("Lignes écrites   : " + stepExecution.getWriteCount());
        rapport.write("Lignes filtrées  : " + stepExecution.getFilterCount());
    }


    protected void writeLine(String ligne) {
        rapport.write(ligne);
    }


    private void writeDuration(StepExecution stepExecution) {

        LocalDateTime startTime = stepExecution.getStartTime();
        LocalDateTime endTime = stepExecution.getEndTime();

        if (startTime != null && endTime != null) {

            Duration duree = Duration.between(
                    startTime.atZone(ZoneId.systemDefault()),
                    endTime.atZone(ZoneId.systemDefault())
            );

            rapport.write(
                    "Temps step       : "
                            + duree.toMinutes() + "min "
                            + duree.toSeconds() + "s "
                            + duree.toMillis() + "ms"
            );
        }
    }
}
