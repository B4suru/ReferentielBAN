package natsystem.processor;

import natsystem.dvf.batch.config.processor.DvfProcessor;
import natsystem.dvf.entity.Dvf;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class DvfProcessorTest {
    @Test
    void shouldCreateDvfValidatingProcessor() {
        DvfProcessor dvfProcessor = new DvfProcessor();

        FileManager logsFileManager = mock(FileManager.class);

        ValidatingItemProcessor<Dvf> processor =
                dvfProcessor.dvfValidatingProcessor(logsFileManager);

        assertNotNull(processor);
    }
}
