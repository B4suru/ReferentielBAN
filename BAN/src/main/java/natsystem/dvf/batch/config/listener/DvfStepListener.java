package natsystem.dvf.batch.config.listener;

import natsystem.shared.listener.AbstractStepListener;
import natsystem.shared.tools.FileManager;
import natsystem.shared.tools.Tool;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;


@Component
public class DvfStepListener extends AbstractStepListener {

    @Override
    protected void afterReport(StepExecution stepExecution) {
        String file = stepExecution.getExecutionContext().getString("file");
        Tool.deleteTempFile(file);
    }

    public DvfStepListener(FileManager rapportFileManager) {
        super(rapportFileManager);
    }
}
