package natsystem.shared.tasklet;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import natsystem.shared.tools.FileLocator;
import natsystem.shared.tools.FileManager;
import natsystem.shared.tools.TimerTool;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
public abstract class AbstractRetrieveFileTasklet implements Tasklet {
    @Value("${dossier.csv}")
    private String dossierCsv;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<Path> listFile = FileLocator.listFile(dossierCsv);
        ExecutionContext context = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution().getExecutionContext();
        context.putString("fileName", "");
        context.putString("file", "");

        if (listFile.isEmpty()){
            contribution.setExitStatus(new ExitStatus("NO_INPUT_FILE"));
            log.info("No input files found");
            return RepeatStatus.FINISHED;
        }
        else if (listFile.size() > 1){
            contribution.setExitStatus(new ExitStatus("MULTIPLE_FILES_FOUND"));
            context.putString("motifEchec", "MULTIPLE_FILES_FOUND");
            log.info("Multiple files found");
            return RepeatStatus.FINISHED;
        }
        else {
            FileManager file = new FileManager();
            String filepath = listFile.getFirst().toString();
            String fileName = listFile.getFirst().getFileName().toString();
            file.setFile(filepath);

            context.putString("fileName", fileName);
            context.putString("file", filepath);

            if (!file.isCsvValid(getExpectedHeader()) && !getExpectedHeader().isEmpty()) {
                contribution.setExitStatus(new ExitStatus("CSV_NOT_VALID"));
                context.putString("motifEchec", "CSV_NOT_VALID");
                log.info("The csv is not valid");
                return RepeatStatus.FINISHED;
            }

            if (isSortRequired()) {
                log.info("-- Début tri csv --");
                TimerTool sortTimer = new TimerTool();
                file.sortCSV();
                log.info("Temps tri csv : " + sortTimer.showTimer());
                log.info("-- Fin tri csv --");
            }
        }
        return null;
    }

    protected boolean isSortRequired() {
        return true;
    }

    protected abstract String getExpectedHeader();
}
