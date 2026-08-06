package natsystem.shared.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public abstract class AbstractSplitDepartmentTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Path outDir = Paths.get("csv");
        Files.createDirectories(outDir);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(getInputFile(chunkContext)))) {

            String header = reader.readLine();
            String line;
            String currentDept = null;
            BufferedWriter writer = null;

            try {
                while ((line = reader.readLine()) != null) {

                    String dept = extractDepartment(line);

                    if (!dept.equals(currentDept)) {

                        if (writer != null) {
                            writer.close();
                        }

                        Path outFile = outDir.resolve(dept + ".csv");
                        writer = Files.newBufferedWriter(outFile);

                        if (header != null) {
                            writer.write(header);
                            writer.newLine();
                        }

                        currentDept = dept;
                    }

                    writer.write(line);
                    writer.newLine();
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
        afterExecution(chunkContext);
        return RepeatStatus.FINISHED;
    }

    protected void afterExecution(ChunkContext chunkContext) {
    }

    protected abstract String getInputFile(ChunkContext chunkContext);

    protected abstract String extractDepartment(String line);
}
