package natSystem.BAN.batch.listener;

import natSystem.BAN.tools.FileManager;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;


@Component
public class MasterStepListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        FileManager rapport = new FileManager(stepExecution.getJobParameters().getString("rapportFileName"));
        FileManager logs = new FileManager(stepExecution.getJobParameters().getString("logFileName"));
        int del = 0;

        rapport.write("********************************** " + stepExecution.getStepName() + "**********************************");
        rapport.write("Lignes total lues      : " + stepExecution.getReadCount());
        rapport.write("Lignes total écrites   : " + stepExecution.getWriteCount());
        rapport.write("Lignes total filtrées  : " + stepExecution.getFilterCount());
        rapport.write("Lignes total supprimé  : " + del);
        rapport.close();
        return stepExecution.getExitStatus();
    }
}
