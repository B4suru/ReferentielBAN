package natSystem.BAN.batch.config.tasklet;

import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.tools.CsvLocator;
import natSystem.BAN.tools.FileManager;
import natSystem.BAN.tools.TimerTool;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class RetrieveCsvTasklet implements Tasklet {
    @Value("${dossier.csv}")
    private String dossierCsv;

    @Value("${param.recuperation:false}")
    private boolean isRecuperationActif;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<Path> fichiersCsv = CsvLocator.listerCsv(dossierCsv);
        ExecutionContext context = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution().getExecutionContext();
        context.putString("csvFileName", "");
        context.putString("csvFile", "");
        context.putString("checksum", "");

        if (fichiersCsv.isEmpty() && isRecuperationActif){
            dowloadCsv();
            fichiersCsv = CsvLocator.listerCsv(dossierCsv);
        }

        if (fichiersCsv.isEmpty()){
            contribution.setExitStatus(new ExitStatus("NO_INPUT_FILE"));
            return RepeatStatus.FINISHED;
        }
        else if (fichiersCsv.size() > 1){
            contribution.setExitStatus(new ExitStatus("MULTIPLE_FILES_FOUND"));
            context.putString("motifEchec", "MULTIPLE_FILES_FOUND");
            return RepeatStatus.FINISHED;
        }
        else {
            FileManager csvFile = new FileManager();
            String file = fichiersCsv.getFirst().toString();
            String fileName = fichiersCsv.getFirst().getFileName().toString();
            csvFile.setFile(file);

            String checksum = CsvLocator.sha256(Path.of(file));

            context.putString("csvFileName", fileName);
            context.putString("csvFile", file);
            context.putString("checksum", checksum);

            if (csvFile.isCsvValid()){
                log.info("-- Début tri csv --");
                TimerTool sortTimer = new TimerTool();
                csvFile.sortCSV();
                log.info("Temps tri csv : "   + sortTimer.showTimer());
                log.info("-- Fin tri csv --");
            } else {
                contribution.setExitStatus(new ExitStatus("CSV_NOT_VALID"));
                context.putString("motifEchec", "CSV_NOT_VALID");
                return RepeatStatus.FINISHED;
            }
        }
        return null;
    }

    private void dowloadCsv(){
        String fileUrl = "https://adresse.data.gouv.fr/data/ban/adresses/2026-06-17/csv/adresses-79.csv.gz";
        try (
                InputStream in = new URL(fileUrl).openStream();
                GZIPInputStream gzip = new GZIPInputStream(in)
        ) {
            Files.copy(gzip, Path.of(dossierCsv + "\\adresses-79.csv"));
        } catch (IOException e) {
            System.err.println("Erreur lors du téléchargement du fichier csv : " + e);
        }
        System.out.println("CSV téléchargé et décompressé !");
    }


}
