package natSystem.BAN.batch.listener;

import natSystem.BAN.tools.FileManager;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobListener implements JobExecutionListener {
    @Override
    public void afterJob (JobExecution jobExecution){

        String path = jobExecution.getExecutionContext().getString("csvFile");
        if (!path.equals("")) {
            FileManager csvFile = new FileManager();
            csvFile.setFile(path);
            csvFile.archiverFichier(jobExecution.getExecutionContext().getString("csvFileName"));
        }

        String motif = jobExecution.getExecutionContext().getString("motifEchec", "");
        FileManager rapport = new FileManager(jobExecution.getJobParameters().getString("rapportFileName"));
        rapport.write("Statut du job          : " + jobExecution.getStatus());
        if (jobExecution.getExitStatus().getExitCode().equals("FAILED") && !motif.isBlank()){
            rapport.write("ExitStatus du job      : " + motif);
        } else {
            rapport.write("ExitStatus du job      : " + jobExecution.getExitStatus().getExitCode());
        }
        rapport.write("Checksum du fichier    : " + jobExecution.getExecutionContext().getString("checksum"));
        if (jobExecution.getExitStatus().getExitCode().equals("NO_INPUT_FILE")) {
            rapport.write("Aucun fichier à traiter");
        }
    }
}
