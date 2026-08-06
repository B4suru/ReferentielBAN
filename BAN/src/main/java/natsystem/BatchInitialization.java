package natsystem;

import natsystem.shared.tools.FileLocator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BatchInitialization {

    private final JobOperator launcher;
    private final Map<String, Job> jobs;

    @Value("${param.recuperation}")
    private boolean isRecuperationActif;

    @Value("${dossier.csv}")
    private String folder;

    public BatchInitialization(JobOperator launcher, Map<String, Job> jobs) {
        this.launcher = launcher;
        this.jobs = jobs;
    }

    public JobExecution executerBatch(String jobName) throws Exception{
        log.info("-- Début du programme : "+ jobName +" --");
        String logFileName = nameLogDatedFile("Logs", jobName);
        String rapportFileName = nameLogDatedFile("Rapport", jobName);



        JobExecution jobExecution = processFile(logFileName, rapportFileName, jobName);

        log.info("-- Fin du programme : "+ jobName +" --");
        return jobExecution;
    }

    private String nameLogDatedFile(String name, String jobName) {
        Path dossier = Path.of(name);
        try {
            Files.createDirectories(dossier);
        } catch (IOException e){
            log.error("Erreur lors de la création du dossier :" + e.getMessage());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        return  name + "/"+ name +  "_" + jobName + "_" + LocalDateTime.now(ZoneId.systemDefault()).format(formatter) + ".txt";

    }

    private JobExecution processFile(String logFileName, String rapportFileName, String jobName) throws Exception {


        String checksum = calculateChecksumFile(jobName);
        if (checksum == null) {
            log.warn("Checksum du fichier non disponible avant lancement (fichier absent ou ambigu) ; "
                    + "la déduplication par checksum ne s'appliquera pas pour cette exécution.");
        }

        log.info("-- Début du traitement du fichier --");
        Job job = jobs.get(jobName);

        if (job == null) {
            throw new IllegalArgumentException("Job inconnu : " + jobName);
        }
        JobParameters params = buildJobParameters(logFileName, rapportFileName, checksum);
        JobExecution jobExecution = launcher.start(job, params);
        log.info("-- Fin du traitement du fichier --");
        return jobExecution;
    }

    private String calculateChecksumFile(String jobName) {
        try {
            List<Path> fichiers = FileLocator.listFile(folder);
            if (fichiers.isEmpty() && isRecuperationActif) {
                if (jobName.equals("jobImportBan")){
                    FileLocator.downloadFile("https://adresse.data.gouv.fr/data/ban/adresses/2026-06-17/csv/adresses-79.csv.gz",
                            folder, "adresses-79.csv");
                } else if (jobName.equals("jobImportDvf")) {
                    FileLocator.downloadFile("https://files.data.gouv.fr/geo-dvf/latest/csv/2025/departements/79.csv.gz",
                            folder, "dvf-79.csv");
                } else if (jobName.equals("jobImportGeoJSON")) {
                    FileLocator.downloadFile("https://adresse.data.gouv.fr/data/contours-administratifs/2023/geojson/communes-100m.geojson.gz",
                            folder, "communes.geojson");
                }
                fichiers = FileLocator.listFile(folder);
                log.info("Téléchargement du fichier ");
            }

            if (!fichiers.isEmpty()){
                return FileLocator.sha256(fichiers.getFirst());
            }
        } catch (Exception e) {
            log.warn("Impossible de calculer le checksum du fichier avant lancement du job : " + e.getMessage());
        }
        return null;
    }

    private JobParameters buildJobParameters(String logFileName, String rapportFileName, String checksum) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addString("logFileName", logFileName, false)
                .addString("rapportFileName", rapportFileName, false);
        if (checksum != null) builder.addString("checksum", checksum);

        return builder.toJobParameters();
    }
}
