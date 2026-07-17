package natSystem.BAN.api.controller;

import lombok.AllArgsConstructor;
import natSystem.BAN.batch.BatchInitialization;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@AllArgsConstructor
@RestController
@RequestMapping("/api/batch")
public class BatchController {
    private final BatchInitialization batchInitialization;
    private final JobRepository jobRepository;

    @GetMapping("/lancer")
    public String startBatch() {
        try {

            JobExecution execution = batchInitialization.executerBatch();

            if (BatchStatus.COMPLETED.equals(execution.getStatus())) {
                return HttpStatus.ACCEPTED.toString();
            }

            return HttpStatus.INTERNAL_SERVER_ERROR.toString();

        }
        catch (JobInstanceAlreadyCompleteException e) {
            return HttpStatus.CONFLICT.toString();
        }
        catch (JobExecutionAlreadyRunningException e) {
            return HttpStatus.LOCKED.toString();
        }
        catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR.toString();
        }
    }

    @GetMapping("/statut/{jobExecutionId}")
    public String getStatusBatchById(@PathVariable Long jobExecutionId){

        JobExecution execution = jobRepository.getJobExecution(jobExecutionId);

        if (execution == null) {
            return HttpStatus.NOT_FOUND.toString();
        }

        return execution.getStatus().name();
    }
}
