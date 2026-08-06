package natsystem.geojson.batch.validator;

import natsystem.geojson.entity.CommunesGeoJson;
import natsystem.shared.tools.FileManager;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;

public class GeoJsonValidator implements Validator<CommunesGeoJson> {
    private final FileManager logs;

    public GeoJsonValidator(FileManager logsFileManager) {
        this.logs = logsFileManager;
    }

    @Override
    public void validate(CommunesGeoJson communesGeoJson) throws ValidationException {
        validateRequiredFields(communesGeoJson);
    }

    private void validateRequiredFields(CommunesGeoJson communesGeoJson) throws ValidationException {
        if (communesGeoJson.getCodeInsee() == null || communesGeoJson.getCodeInsee().isBlank()) {
            error("Le code insee ne peut pas être vide");
        }

        if(communesGeoJson.getGeometry() == null || communesGeoJson.getGeometry().isBlank()) {
            error("Le données géométrique ne peuvent pas être vide : " + communesGeoJson.getCodeInsee());
        }
    }

    private void error(String message) throws ValidationException {
        logs.write(message);
        throw new ValidationException(message);
    }
}
