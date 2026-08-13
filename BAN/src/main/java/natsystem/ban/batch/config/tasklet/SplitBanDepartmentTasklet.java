package natsystem.ban.batch.config.tasklet;

import natsystem.shared.tasklet.AbstractSplitDepartmentTasklet;
import natsystem.shared.tools.Tool;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SplitBanDepartmentTasklet extends AbstractSplitDepartmentTasklet {
    private final Set<String> usedDepts = new HashSet<>();

    @Override
    public String getInputFile(ChunkContext chunkContext) {
        return "csv_sorted.csv";
    }

    @Override
    public String extractDepartment(String line) {
        String id = line.split(";", 2)[0];
        String dept;

        if (id.startsWith("97") || id.startsWith("98")) {
            dept = id.substring(0, 3);
        } else {
            dept = id.substring(0, 2);
        }
        usedDepts.add(dept);
        return dept;
    }

    @Override
    public void afterExecution(ChunkContext chunkContext)  {
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("usedDepts", usedDepts);

        Tool.deleteTempFile("csv_sorted.csv");
    }
}
