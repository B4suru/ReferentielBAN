package natSystem.BAN.batch;

import natSystem.BAN.tools.CsvLocator;
import natSystem.BAN.tools.FileManager;
import natSystem.BAN.tools.TimerTool;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class BatchInitialization {

    private final JobOperator launcher;
    private final Job banBatchJob;

    @Value("${filtre.codePostal}")
    private Long codePostal;

    @Value("${filtre.codeInsee}")
    private String codeInsee;

    @Value("${dossier.csv}")
    private String dossierCsv;

    public BatchInitialization(JobOperator launcher, Job banBatchJob) {
        this.launcher = launcher;
        this.banBatchJob = banBatchJob;
    }

    public JobExecution executerBatch() throws Exception{
        log.info("-- Début du programme --");
        FileManager logs = createLogDatedFile("Logs");
        FileManager rapport = createLogDatedFile("Rapport");

        JobExecution jobExecution = processFile(logs, rapport);

        log.info("Chemin du fichier de logs : " + logs.getAbsolutePath());
        log.info("Chemin du fichier de rapport : " + rapport.getAbsolutePath());
        logs.close();
        rapport.close();
        log.info("-- Fin du programme --");
        return jobExecution;
    }

    private FileManager createLogDatedFile(String name) {
        Path dossier = Path.of(name);
        try {
            Files.createDirectories(dossier);
        } catch (IOException e){
            System.err.println("Erreur lors de la création du dossier :" + e.getMessage());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String logFileName = name + "/"+ name +"(" + LocalDateTime.now().format(formatter) + ").txt";
        return new FileManager(logFileName);
    }

    private JobExecution processFile(FileManager logs, FileManager rapport) throws Exception {
        TimerTool batchTimer = new TimerTool();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH'h'mm ss's'");
        rapport.write("Date : "    + batchTimer.getStart().format(formatter));
        rapport.write("Filtre : [Code postal : " + codePostal + "] | [Code insee: " + codeInsee + "]");


        String checksum = calculerChecksumCsv();
        if (checksum != null) {
            rapport.write("Checksum CSV : " + checksum);
        } else {
            log.warn("Checksum du CSV non disponible avant lancement (fichier absent ou ambigu) ; "
                    + "la déduplication par checksum ne s'appliquera pas pour cette exécution.");
        }


        log.info("-- Début du traitement du fichier --");
        JobParameters params = buildJobParameters(codePostal, codeInsee, logs.getFile(), rapport.getFile(), checksum);
        JobExecution jobExecution = launcher.start(banBatchJob, params);
        rapport.write("Durée traitement : " + batchTimer.showTimer());
        log.info("-- Fin du traitement du fichier --");
        return jobExecution;
    }


    private String calculerChecksumCsv() {
        try {
            List<Path> fichiers = CsvLocator.listerCsv(dossierCsv);
            if (fichiers.size() == 1) {
                return CsvLocator.sha256(fichiers.getFirst());
            }
        } catch (Exception e) {
            log.warn("Impossible de calculer le checksum du CSV avant lancement du job : " + e.getMessage());
        }
        return null;
    }

    private JobParameters buildJobParameters( Long codePostal, String codeInsee, String logFileName, String rapportFileName, String checksum) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis(), false)
                .addString("logFileName", logFileName, false)
                .addString("rapportFileName", rapportFileName, false);

        if (codePostal != null) builder.addLong("codePostal", codePostal);
        if (codeInsee  != null) builder.addString("codeInsee",  codeInsee);
        if (checksum   != null) builder.addString("checksum", checksum);
        return builder.toJobParameters();
    }
}
