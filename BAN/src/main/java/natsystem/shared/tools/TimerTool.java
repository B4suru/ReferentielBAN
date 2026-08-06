package natsystem.shared.tools;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Outil permettant de mesurer le temps écoulé depuis une date de début.
 * Par défaut, le chronomètre démarre à l'instant de l'instanciation de l'objet.
 * Il est également possible de fournir une date de début personnalisée.
 */
@Getter
@Setter
public class TimerTool {
    private LocalDateTime start;

    public TimerTool() {
        this.start = LocalDateTime.now(ZoneId.systemDefault());
    }

    public TimerTool(LocalDateTime start){
        this.start = start;
    }

    public String showTimer(){
        LocalDateTime end = LocalDateTime.now(ZoneId.systemDefault());
        Duration dureeSort = Duration.between(start.atZone(ZoneId.systemDefault()), end.atZone(ZoneId.systemDefault()));
        long minutesSort = dureeSort.toMinutes();
        long secondesSort = dureeSort.minusMinutes(minutesSort).getSeconds();
        return minutesSort + " min "+ secondesSort+" sec";
    }
}
