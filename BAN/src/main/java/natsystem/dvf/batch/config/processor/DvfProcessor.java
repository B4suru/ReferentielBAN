package natsystem.dvf.batch.config.processor;


import natsystem.dvf.batch.validator.DvfRowValidator;
import natsystem.dvf.entity.Dvf;
import natsystem.shared.tools.FileManager;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DvfProcessor {

    @Bean
    @StepScope
    public ValidatingItemProcessor<Dvf> dvfValidatingProcessor(
            FileManager logsFileManager)
    {
        ValidatingItemProcessor<Dvf> validator = new ValidatingItemProcessor<>(new DvfRowValidator(logsFileManager));
        validator.setFilter(true);
        return validator;
    }
}
