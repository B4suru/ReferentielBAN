package natsystem.dvf.batch.config.tasklet;

import natsystem.shared.tasklet.AbstractSplitDepartmentTasklet;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class SplitDvfDepartmentTalklet extends AbstractSplitDepartmentTasklet {

    @Override
    protected String getInputFile(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .getString("file");
    }

    @Override
    protected String extractDepartment(String line) {
        return line.split(",")[12];
    }

}
