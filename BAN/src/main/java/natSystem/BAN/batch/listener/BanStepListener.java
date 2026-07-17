package natSystem.BAN.batch.listener;

import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.batch.context.BanDiffContext;
import natSystem.BAN.entity.Ban;
import natSystem.BAN.tools.TimerTool;
import natSystem.BAN.tools.Tool;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import natSystem.BAN.tools.FileManager;

import java.time.Duration;

@Slf4j
@Component
public class BanStepListener implements StepExecutionListener, ChunkListener<Ban, Ban> {
    private final BanDiffContext banDiffContext;
    private StepExecution stepExecution;
    private final JdbcTemplate jdbc;

    public BanStepListener(JdbcTemplate jdbc, BanDiffContext banDiffContext) {
        this.banDiffContext = banDiffContext;
        this.jdbc = jdbc;
    }

    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;


    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        FileManager rapport = new FileManager(stepExecution.getJobParameters().getString("rapportFileName"));
        FileManager logs = new FileManager(stepExecution.getJobParameters().getString("logFileName"));

        int del = 0;
        for (String id : banDiffContext.getBdIds()) {
            jdbc.update("DELETE FROM ban WHERE id = ?", id);
            logs.write("Supprimé : " + id);
            del ++;
        }


        String fileName = stepExecution.getExecutionContext().getString("file");
        Duration duree = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime());
        Tool.deleteTempFile(fileName);
        rapport.write("-------- " + stepExecution.getStepName() + " --------");
        rapport.write("Lignes lues      : " + stepExecution.getReadCount());
        rapport.write("Lignes écrites   : " + stepExecution.getWriteCount());
        rapport.write("Lignes filtrées  : " + stepExecution.getFilterCount());
        rapport.write("Lignes supprimé  : " + del);
        rapport.write("Temps step       : " + duree.toMinutes() + "min " + duree.toSeconds() + "s " + duree.toMillis() + "ms");

        rapport.close();
        return stepExecution.getExitStatus();
    }

    /*public void afterChunk(Chunk chunk) {
        long traitees = stepExecution.getReadCount();
        long totalLignes = stepExecution.getExecutionContext().getLong("fileNbLine");
        String nomStep = stepExecution.getStepName();
        TimerTool timerStep = new TimerTool(stepExecution.getStartTime());
        int pourcentage = (int) ((traitees * 100) / totalLignes);
        System.out.print("\rTraitement en cours ( "+ nomStep +" ) : " + pourcentage + "% ( " + traitees + " / " + totalLignes + " ) " + timerStep.showTimer());
    }*/
}
