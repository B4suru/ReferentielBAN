package natSystem.BAN.batch.config.processor;

import natSystem.BAN.batch.context.BanDiffContext;
import natSystem.BAN.batch.validator.RowValidator;
import natSystem.BAN.entity.Ban;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class BanProcessor {
    @Bean
    @StepScope
    public CompositeItemProcessor<Ban, Ban> compositeProcessor(
            ValidatingItemProcessor<Ban> validatingProcessor,
            ItemProcessor<Ban, Ban> processorFilter,
            ItemProcessor<Ban, Ban> processorDiff
    ) {
        CompositeItemProcessor<Ban, Ban> composite = new CompositeItemProcessor<>();
        composite.setDelegates(Arrays.asList(validatingProcessor, processorFilter, processorDiff));
        return composite;
    }

    @Bean
    @StepScope
    public ItemProcessor<Ban, Ban> processorFilter(
            @Value("#{jobParameters['codePostal']}") Integer codePostal,
            @Value("#{jobParameters['codeInsee']}") Integer codeInsee
    ) {
        return ban -> {
            if (codePostal == null && codeInsee == null) {
                return ban;
            }

            boolean match = true;
            if (codePostal != null) {
                match &= codePostal.equals(ban.getCodePostal());
            }
            if (codeInsee != null) {
                match &= codeInsee.equals(ban.getCodeInsee());
            }

            return match ? ban : null;
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Ban, Ban> processorDiff(BanDiffContext banDiffContext) {
        return ban -> {
            if (banDiffContext.getBdIds().remove(ban.getId())) {
                return null;
            } else {
                return ban;
            }
        };
    }

    @Bean
    @StepScope
    public ValidatingItemProcessor<Ban> validatingProcessor(
            @Value("#{jobParameters['logFileName']}") String logFileName)
    {
        ValidatingItemProcessor<Ban> validator = new ValidatingItemProcessor<>(new RowValidator(logFileName));
        validator.setFilter(true);
        return validator;
    }
}
