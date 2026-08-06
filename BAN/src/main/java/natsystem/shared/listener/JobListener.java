package natsystem.shared.listener;

import lombok.extern.slf4j.Slf4j;
import natsystem.shared.tools.FileManager;
import natsystem.shared.tools.Tool;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class JobListener implements JobExecutionListener {
    private final FileManager rapport;
    private final FileManager logs;

    public JobListener(FileManager rapportFileManager, FileManager logsFileManager) {
        this.rapport = rapportFileManager;
        this.logs = logsFileManager;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

        String rapportFileName = jobExecution.getJobParameters().getString("rapportFileName");
        String logFileName = jobExecution.getJobParameters().getString("logFileName");

        rapport.open(rapportFileName);
        logs.open(logFileName);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH'h'mm ss's'");
        rapport.write("Date : " + LocalDateTime.now(ZoneId.systemDefault()).format(formatter));
        String checksum = jobExecution.getJobParameters().getString("checksum");
        if (checksum != null) {
            rapport.write("Checksum du fichier : " + checksum);
        }


    }

    @Override
    public void afterJob (JobExecution jobExecution){

        String path = jobExecution.getExecutionContext().getString("file");
        if (!path.isEmpty()) {
            FileManager csvFile = new FileManager();
            csvFile.setFile(path);
            csvFile.archiverFichier(jobExecution.getExecutionContext().getString("fileName"));
        }

        String motif = jobExecution.getExecutionContext().getString("motifEchec", "");
        rapport.write("Statut du job          : " + jobExecution.getStatus());
        if (jobExecution.getExitStatus().getExitCode().equals("FAILED") && !motif.isBlank()){
            rapport.write("ExitStatus du job      : " + motif);
        } else {
            rapport.write("ExitStatus du job      : " + jobExecution.getExitStatus().getExitCode());
        }
        if (jobExecution.getExitStatus().getExitCode().equals("NO_INPUT_FILE")) {
            rapport.write("Aucun fichier à traiter");
        }

        log.info("Chemin du fichier de logs : " + logs.getAbsolutePath());
        log.info("Chemin du fichier de rapport : " + rapport.getAbsolutePath());

        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        if (startTime != null && endTime != null) {

            Duration duree = Duration.between(
                    startTime.atZone(ZoneId.systemDefault()),
                    endTime.atZone(ZoneId.systemDefault())
            );
            rapport.write("Durée traitement       : " + duree.toMinutes() + "min " + duree.toSeconds() + "s");
        }

        rapport.close();
        logs.close();
    }
}
