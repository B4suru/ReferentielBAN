package natsystem.api.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import natsystem.BatchInitialization;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/batch")
public class BatchController {
    private final BatchInitialization batchInitialization;
    private final JobRepository jobRepository;

    @PostMapping("/lancer/{job}")
    public String startBatch(@PathVariable String job) {
        try {
            JobExecution execution = batchInitialization.executerBatch(job);
            if (BatchStatus.COMPLETED.equals(execution.getStatus())) {
                return HttpStatus.ACCEPTED.toString();
            }

            return HttpStatus.INTERNAL_SERVER_ERROR.toString();
        }
        catch (JobInstanceAlreadyCompleteException _) {
            return HttpStatus.CONFLICT.toString();
        }
        catch (JobExecutionAlreadyRunningException _) {
            return HttpStatus.LOCKED.toString();
        }
        catch (Exception e) {
            log.error(e.getMessage());
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
