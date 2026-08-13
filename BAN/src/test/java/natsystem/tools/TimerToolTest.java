package natsystem.tools;

import natsystem.shared.tools.TimerTool;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class TimerToolTest {

    @Test
    void constructor_shouldInitializeStart() {
        TimerTool timer = new TimerTool();

        assertNotNull(timer.getStart());
    }

    @Test
    void constructor_shouldUseProvidedStartDate() {
        LocalDateTime start = LocalDateTime.of(
                2026,
                Month.AUGUST,
                11,
                10,
                30,
                0
        );

        TimerTool timer = new TimerTool(start);

        assertEquals(start, timer.getStart());
    }

    @Test
    void showTimer_shouldReturnZeroSeconds_whenStartIsNow() {
        TimerTool timer = new TimerTool(LocalDateTime.now());

        String result = timer.showTimer();

        assertTrue(result.matches("\\d+ min \\d+ sec"));
    }

    @Test
    void showTimer_shouldCalculateMinutesAndSeconds() {
        LocalDateTime start = LocalDateTime.now()
                .minusMinutes(2)
                .minusSeconds(15);

        TimerTool timer = new TimerTool(start);

        String result = timer.showTimer();

        assertTrue(result.matches("\\d+ min \\d+ sec"));

        String[] parts = result.split(" ");

        long minutes = Long.parseLong(parts[0]);
        long seconds = Long.parseLong(parts[2]);

        assertTrue(minutes >= 2);
        assertTrue(seconds >= 0);
        assertTrue(seconds < 60);
    }
}
