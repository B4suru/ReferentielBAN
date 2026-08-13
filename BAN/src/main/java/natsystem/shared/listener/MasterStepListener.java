package natsystem.shared.listener;

import natsystem.shared.tools.FileManager;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;


@Component
public class MasterStepListener implements StepExecutionListener {
    private final FileManager rapport;

    public MasterStepListener(FileManager rapportFileManager) {
        this.rapport = rapportFileManager;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long totalInsere = 0;
        long totalMisAJour = 0;
        long totalSupprime = 0;

        for (StepExecution se : stepExecution.getJobExecution().getStepExecutions()) {
            if (se.getStepName().startsWith("banBatchStep:")) {
                totalInsere += se.getExecutionContext().getLong("nbInserted", 0);
                totalMisAJour += se.getExecutionContext().getLong("nbUpdated", 0);
                totalSupprime += se.getExecutionContext().getLong("nbDeleted", 0);
            }
        }


        rapport.write("======== TOTAL (toutes partitions) ========");
        rapport.write("Lignes total lues      : " + stepExecution.getReadCount());
        rapport.write("Lignes total écrites   : " + stepExecution.getWriteCount());
        rapport.write("Lignes total filtrées  : " + stepExecution.getFilterCount());
        rapport.write("Insertions             : " + totalInsere);
        rapport.write("Mises à jour           : " + totalMisAJour);
        rapport.write("Suppressions           : " + totalSupprime);
        return stepExecution.getExitStatus();
    }
}
