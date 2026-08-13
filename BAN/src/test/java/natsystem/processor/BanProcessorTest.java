package natsystem.processor;

import natsystem.ban.batch.config.processor.BanProcessor;
import natsystem.ban.batch.context.BanDiffContext;
import natsystem.ban.entity.Ban;
import natsystem.shared.enumeration.Operation;
import natsystem.shared.tools.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class BanProcessorTest {
    private BanProcessor banProcessor;

    @Mock
    private FileManager logsFileManager;

    @BeforeEach
    void setUp() {
        banProcessor = new BanProcessor();
    }

    @Test
    void shouldReturnBanWhenNoFilterIsConfigured() throws Exception {
        banProcessor.codePostal = null;
        banProcessor.codeInsee = "";

        Ban ban = new Ban();
        ban.setCodePostal(75001);
        ban.setCodeInsee("75101");

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertSame(ban, result);
    }

    @Test
    void shouldReturnBanWhenCodePostalMatches() throws Exception {
        banProcessor.codePostal = 75001;
        banProcessor.codeInsee = "";

        Ban ban = new Ban();
        ban.setCodePostal(75001);

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertSame(ban, result);
    }

    @Test
    void shouldFilterBanWhenCodePostalDoesNotMatch() throws Exception {
        banProcessor.codePostal = 75001;
        banProcessor.codeInsee = "";

        Ban ban = new Ban();
        ban.setCodePostal(69001);

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertNull(result);
    }

    @Test
    void shouldReturnBanWhenCodeInseeMatches() throws Exception {
        banProcessor.codePostal = null;
        banProcessor.codeInsee = "75101";

        Ban ban = new Ban();
        ban.setCodeInsee("75101");

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertSame(ban, result);
    }

    @Test
    void shouldFilterBanWhenCodeInseeDoesNotMatch() throws Exception {
        banProcessor.codePostal = null;
        banProcessor.codeInsee = "75101";

        Ban ban = new Ban();
        ban.setCodeInsee("69001");

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertNull(result);
    }

    @Test
    void shouldReturnBanWhenBothFiltersMatch() throws Exception {
        banProcessor.codePostal = 75001;
        banProcessor.codeInsee = "75101";

        Ban ban = new Ban();
        ban.setCodePostal(75001);
        ban.setCodeInsee("75101");

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertSame(ban, result);
    }

    @Test
    void shouldFilterBanWhenOnlyOneFilterMatches() throws Exception {
        banProcessor.codePostal = 75001;
        banProcessor.codeInsee = "75101";

        Ban ban = new Ban();
        ban.setCodePostal(75001);
        ban.setCodeInsee("69001");

        var processor = banProcessor.banProcessorFilter();

        Ban result = processor.process(ban);

        assertNull(result);
    }

    @Test
    void shouldCreateInsertWhenBanDoesNotExistInDatabase() throws Exception {
        HashMap<String, String> bdMap = new HashMap<>();

        AtomicInteger insertCount = new AtomicInteger();
        AtomicInteger updateCount = new AtomicInteger();

        BanDiffContext context = mock(BanDiffContext.class);

        when(context.getBdMap()).thenReturn(bdMap);
        when(context.getInsertCount()).thenReturn(insertCount);
        when(context.getUpdateCount()).thenReturn(updateCount);

        Ban ban = new Ban();
        ban.setId("123");
        ban.setHash("hash-1");

        var processor = banProcessor.banProcessorDiff(context);

        Ban result = processor.process(ban);

        assertSame(ban, result);
        assertEquals(Operation.INSERT, result.getOperation());
        assertEquals(1, insertCount.get());
        assertEquals(0, updateCount.get());
    }

    @Test
    void shouldCreateUpdateWhenBanExistsWithDifferentHash() throws Exception {
        HashMap<String, String> bdMap = new HashMap<>();
        bdMap.put("123", "old-hash");

        AtomicInteger insertCount = new AtomicInteger();
        AtomicInteger updateCount = new AtomicInteger();

        BanDiffContext context = mock(BanDiffContext.class);

        when(context.getBdMap()).thenReturn(bdMap);
        when(context.getInsertCount()).thenReturn(insertCount);
        when(context.getUpdateCount()).thenReturn(updateCount);

        Ban ban = new Ban();
        ban.setId("123");
        ban.setHash("new-hash");

        var processor = banProcessor.banProcessorDiff(context);

        Ban result = processor.process(ban);

        assertSame(ban, result);
        assertEquals(Operation.UPDATE, result.getOperation());
        assertEquals(0, insertCount.get());
        assertEquals(1, updateCount.get());

        assertFalse(bdMap.containsKey("123"));
    }

    @Test
    void shouldFilterBanWhenHashIsIdentical() throws Exception {
        HashMap<String, String> bdMap = new HashMap<>();
        bdMap.put("123", "same-hash");

        AtomicInteger insertCount = new AtomicInteger();
        AtomicInteger updateCount = new AtomicInteger();

        BanDiffContext context = mock(BanDiffContext.class);

        when(context.getBdMap()).thenReturn(bdMap);
        when(context.getInsertCount()).thenReturn(insertCount);
        when(context.getUpdateCount()).thenReturn(updateCount);

        Ban ban = new Ban();
        ban.setId("123");
        ban.setHash("same-hash");

        var processor = banProcessor.banProcessorDiff(context);

        Ban result = processor.process(ban);

        assertNull(result);
        assertEquals(0, insertCount.get());
        assertEquals(0, updateCount.get());

        assertFalse(bdMap.containsKey("123"));
    }

    @Test
    void shouldRemoveExistingBanFromMap() throws Exception {
        HashMap<String, String> bdMap = new HashMap<>();
        bdMap.put("123", "old-hash");

        AtomicInteger insertCount = new AtomicInteger();
        AtomicInteger updateCount = new AtomicInteger();

        BanDiffContext context = mock(BanDiffContext.class);

        when(context.getBdMap()).thenReturn(bdMap);
        when(context.getInsertCount()).thenReturn(insertCount);
        when(context.getUpdateCount()).thenReturn(updateCount);

        Ban ban = new Ban();
        ban.setId("123");
        ban.setHash("new-hash");

        var processor = banProcessor.banProcessorDiff(context);

        processor.process(ban);

        assertFalse(bdMap.containsKey("123"));
    }

    @Test
    void shouldCreateValidatingProcessor() {
        var processor = banProcessor.banValidatingProcessor(logsFileManager);

        assertNotNull(processor);
    }

}
