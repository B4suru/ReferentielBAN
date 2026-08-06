package natsystem.geojson.batch.config.listener;
import natsystem.shared.listener.AbstractStepListener;
import natsystem.shared.tools.FileManager;
import org.springframework.stereotype.Component;

@Component
public class GeoJsonStepListener extends AbstractStepListener {
    public GeoJsonStepListener(FileManager rapportFileManager) {
        super(rapportFileManager);
    }
}
