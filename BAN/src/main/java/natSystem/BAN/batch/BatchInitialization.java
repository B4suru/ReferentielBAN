package natSystem.BAN.batch;

import natSystem.BAN.tools.FileManager;
import natSystem.BAN.tools.ScannerTool;
import natSystem.BAN.tools.TimerTool;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class BatchInitialization {

    private final JobOperator launcher;
    private final Job banBatchJob;

    public BatchInitialization(JobOperator launcher, Job banBatchJob) {
        this.launcher = launcher;
        this.banBatchJob = banBatchJob;
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            log.info("-- Début du programme --");

            FileManager logs = initLogs();
            ScannerTool scanner = new ScannerTool();
            boolean isNotFinished = true;

            logs.write("============================ Debuts logs ============================");
            do {
                isNotFinished = processFile(launcher, banBatchJob, logs, scanner);
            } while (isNotFinished);
            logs.write("============================  Fin logs   ============================");

            log.info("Chemin du fichier de logs : " + logs.getAbsolutePath());
            logs.close();
            scanner.close();
            log.info("-- Fin du programme --");
        };
    }

    private FileManager initLogs() {
        Path dossierLogs = Path.of("Logs");
        try {
            Files.createDirectories(dossierLogs);
        } catch (IOException e){
            System.err.println("Erreur lors de la création du dossier :" + e.getMessage());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSS");
        String logFileName = "Logs/Logs(" + LocalDateTime.now().format(formatter) + ").txt";
        return new FileManager(logFileName);
    }

    private boolean processFile(JobOperator launcher, Job banBatchJob, FileManager logs, ScannerTool scanner) throws Exception {
        // ---- Sélection du fichier CSV ----
        log.info("-- Début sélection utilisateur --");
        FileManager csvFile = selectCsvFile(scanner);
        Long codePostalInput = scanner.getLong("Code postal (laisser vide pour ignorer) : ");
        Long codeInseeInput  = scanner.getLong("Code INSEE (laisser vide pour ignorer) : ");
        log.info("-- Fin sélection utilisateur --");

        // ---- Logs de début ----
        TimerTool batchTimer = new TimerTool();
        logs.write("Date : "    + batchTimer.getStart());
        logs.write("Fichier : " + csvFile.getFileName());
        logs.write("Filtre : [Code postal : " + codePostalInput + "] | [Code insee: " + codeInseeInput + "]");

        sortCsvFile(csvFile, logs);

        // ---- Lancement du job ----
        log.info("-- Début du traitement du fichier --");
        JobParameters params = buildJobParameters(csvFile, codePostalInput, codeInseeInput, logs.getFileName());
        launcher.start(banBatchJob, params);
        logs.write("Durée traitement : " + batchTimer.showTimer());
        log.info("-- Fin du traitement du fichier --");

        deleteTempFile();
        return askContinue(scanner);
    }

    private FileManager selectCsvFile(ScannerTool scanner) {
        FileManager csvFile = new FileManager();
        do {
            String path = scanner.getString("Chemin du fichier csv : ");

            //Permet de supprimer les " automatiquement (ex ctrl maj c de fichier : "C:\Users\...\...\fichier.csv" -> C:\Users\...\...\fichier.csv)
            path = path.replaceAll("^\"|\"$", "");
            csvFile.setFileName(path);
        } while (!csvFile.isCSVExisting() || csvFile.getFileName().isEmpty());
        return csvFile;
    }

    private void sortCsvFile(FileManager csvFile, FileManager logs) {
        log.info("-- Début tri csv --");
        TimerTool sortTimer = new TimerTool();
        csvFile.sortCSV();
        log.info("Temps tri csv : "   + sortTimer.showTimer());
        logs.write("Temps tri csv : " + sortTimer.showTimer());
        log.info("-- Fin tri csv --");
    }

    private JobParameters buildJobParameters(FileManager csvFile, Long codePostal, Long codeInsee, String logFileName) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addLong("sizeCSV", csvFile.countFileLine())
                .addString("logFileName", logFileName);

        if (codePostal != null) builder.addLong("codePostal", codePostal);
        if (codeInsee  != null) builder.addLong("codeInsee",  codeInsee);

        return builder.toJobParameters();
    }

    private void deleteTempFile() {
        try {
            Files.delete(Path.of("csv_sorted.csv"));
        } catch (IOException e) {
            System.err.println("Erreur suppression fichier temporaire : " + e.getMessage());
        }
    }

    private boolean askContinue(ScannerTool scanner) {
        String res;
        do {
            res = scanner.getString("Voulez-vous traiter un autre fichier ? (y/n) : ").toLowerCase();
        } while (!res.equals("y") && !res.equals("n"));

        if (res.equals("n")) {
            System.out.println("Fin de traitement des fichiers ...");
            return false;
        }
        return true;
    }
}
