package natsystem.geojson.batch.config.tasklet;

import natsystem.shared.tasklet.AbstractRetrieveFileTasklet;
import org.springframework.stereotype.Component;

@Component
public class RetrieveGeoJsonTasklet extends AbstractRetrieveFileTasklet {

    @Override
    protected boolean isSortRequired() {
        return false;
    }

    @Override
    protected String getExpectedHeader() {
        return "";
    }
}
