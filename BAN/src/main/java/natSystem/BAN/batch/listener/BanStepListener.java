package natSystem.BAN.batch.listener;

import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.batch.context.BanDiffContext;
import natSystem.BAN.entity.Ban;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import natSystem.BAN.tools.FileManager;

@Slf4j
@Component
public class BanStepListener implements StepExecutionListener, ChunkListener<Ban, Ban> {
    private final JdbcTemplate jdbc;
    private final BanDiffContext banDiffContext;
    private StepExecution stepExecution;


    public BanStepListener(JdbcTemplate jdbc, BanDiffContext banDiffContext) {
        this.jdbc = jdbc;
        this.banDiffContext = banDiffContext;
    }

    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Traitement fini : 100%");

        FileManager logs = new FileManager("Logs.txt");
        int del = 0;
        for (String id : banDiffContext.getBdIds()) {
            jdbc.update("DELETE FROM ban WHERE id = ?", id);
            logs.write("Supprimé : " + id);
            del ++;
        }
		logs.write("Lignes lues      : " + stepExecution.getReadCount());
        logs.write("Lignes écrites   : " + stepExecution.getWriteCount());
        logs.write("Lignes filtrées  : " + stepExecution.getFilterCount());
        logs.write("Lignes supprimé  : " + del);

        return stepExecution.getExitStatus();
    }
    public void afterChunk(Chunk chunk) {

        long totalLignes = stepExecution.getJobParameters().getLong("sizeCSV");
        long traitees = stepExecution.getReadCount();
        int pourcentage = (int) ((traitees * 100) / totalLignes);
        System.out.print("\rTraitement en cours : " + pourcentage + "% ( " + traitees + " / " + totalLignes + " )");
    }

}
