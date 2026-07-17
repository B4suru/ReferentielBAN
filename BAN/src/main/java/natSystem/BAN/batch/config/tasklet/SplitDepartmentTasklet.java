package natSystem.BAN.batch.config.tasklet;

import natSystem.BAN.tools.Tool;
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
public class SplitDepartmentTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Path outDir = Paths.get("csv");
        Files.createDirectories(outDir);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("csv_sorted.csv"))) {
            String header = reader.readLine();
            String line;
            String currentDept = null;
            BufferedWriter writer = null;

            while ((line = reader.readLine()) != null) {
                String id = line.split(";", 2)[0];
                String dept = extractDepartement(id);

                if (!dept.equals(currentDept)) {
                    if (writer != null) writer.close();
                    Path outFile = outDir.resolve("ban_" + dept + ".csv");
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
            if (writer != null) writer.close();
        }

        Tool.deleteTempFile("csv_sorted.csv");
        return RepeatStatus.FINISHED;
    }

    private String extractDepartement(String codeInsee) {
        if (codeInsee.startsWith("97") || codeInsee.startsWith("98")) {
            return codeInsee.substring(0, 3); // DROM/COM
        }
        return codeInsee.substring(0, 2);
    }
}
