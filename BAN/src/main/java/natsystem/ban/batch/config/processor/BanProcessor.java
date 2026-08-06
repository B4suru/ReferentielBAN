package natsystem.ban.batch.config.processor;

import natsystem.ban.batch.context.BanDiffContext;
import natsystem.ban.batch.validator.BanRowValidator;
import natsystem.ban.entity.Ban;
import natsystem.shared.enumeration.Operation;
import natsystem.shared.tools.FileManager;
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
    @Value("${filtre.codePostal}")
    private Long codePostal;

    @Value("${filtre.codeInsee}")
    private String codeInsee;

    @Bean
    @StepScope
    public CompositeItemProcessor<Ban, Ban> banCompositeProcessor(
            ValidatingItemProcessor<Ban> banValidatingProcessor,
            ItemProcessor<Ban, Ban> banProcessorFilter,
            ItemProcessor<Ban, Ban> banProcessorDiff
    ) {
        CompositeItemProcessor<Ban, Ban> composite = new CompositeItemProcessor<>();
        composite.setDelegates(Arrays.asList(banValidatingProcessor, banProcessorFilter, banProcessorDiff));
        return composite;
    }

    @Bean
    @StepScope
    public ItemProcessor<Ban, Ban> banProcessorFilter() {
        return ban -> {
            if (codePostal == null && codeInsee.isEmpty()) {
                return ban;
            }

            boolean match = true;
            if (codePostal != null) {
                match &= codePostal.equals(ban.getCodePostal());
            }
            if (!codeInsee.isEmpty()) {
                match &= codeInsee.equals(ban.getCodeInsee());
            }

            return match ? ban : null;
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Ban, Ban> banProcessorDiff(BanDiffContext banDiffContext) {
        return ban -> {
            String existing = banDiffContext.getBdMap().remove(ban.getId());

            if (existing == null) {
                banDiffContext.getInsertCount().incrementAndGet();
                ban.setOperation(Operation.INSERT);
                return ban;
            }

            if (!existing.equals(ban.getHash())) {
                banDiffContext.getUpdateCount().incrementAndGet();
                ban.setOperation(Operation.UPDATE);
                return ban;
            }
            return null;
        };
    }

    @Bean
    @StepScope
    public ValidatingItemProcessor<Ban> banValidatingProcessor(
            FileManager logsFileManager)
    {
        ValidatingItemProcessor<Ban> validator = new ValidatingItemProcessor<>(new BanRowValidator(logsFileManager));
        validator.setFilter(true);
        return validator;
    }
}
