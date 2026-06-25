package natSystem.BAN.batch.stepListener;

import natSystem.BAN.batch.context.BanDiffContext;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import natSystem.BAN.tools.file.File;

@Component
public class BanStepListener implements StepExecutionListener {
    private final JdbcTemplate jdbc;
    private final BanDiffContext banDiffContext;

    public BanStepListener(JdbcTemplate jdbc, BanDiffContext banDiffContext) {
        this.jdbc = jdbc;
        this.banDiffContext = banDiffContext;
    }

    public ExitStatus afterStep(StepExecution stepExecution) {
        File logs = new File("Logs.txt");
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
}
