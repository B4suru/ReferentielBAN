package natSystem.BAN.batch.stepListener;

import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import natSystem.BAN.file.File;

@Component
public class BanStepListener implements StepExecutionListener {
    public ExitStatus afterStep(StepExecution stepExecution) {
    	File logs = new File("Logs.txt");
		logs.write("Lignes lues      : " + stepExecution.getReadCount());
        logs.write("Lignes écrites   : " + stepExecution.getWriteCount());
        logs.write("Lignes filtrées  : " + stepExecution.getFilterCount());
        return stepExecution.getExitStatus();
    }
}
