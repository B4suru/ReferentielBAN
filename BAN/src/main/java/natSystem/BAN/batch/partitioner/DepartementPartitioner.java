package natSystem.BAN.batch.partitioner;


import natSystem.BAN.tools.FileManager;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Component
public class DepartementPartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<>();
        File[] files = new File("csv").listFiles((d, name) -> name.startsWith("ban_") && name.endsWith(".csv"));

        if (files != null) {
            for (File file : files) {
                String dept = file.getName().replace("ban_", "").replace(".csv", "");
                ExecutionContext context = new ExecutionContext();

                FileManager fileManager = new FileManager();
                fileManager.setFile(file.getAbsolutePath());

                context.putString("file", file.getAbsolutePath());
                context.putLong("fileNbLine", fileManager.countFileLine());
                context.putString("departement", dept);
                partitions.put("partition-" + dept, context);
            }
        }
        return partitions;
    }
}
