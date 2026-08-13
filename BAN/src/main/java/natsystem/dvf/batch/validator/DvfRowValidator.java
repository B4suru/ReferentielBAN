package natsystem.dvf.batch.validator;

import natsystem.dvf.entity.Dvf;
import natsystem.shared.tools.FileManager;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;

public class DvfRowValidator implements Validator<Dvf> {
    private final FileManager logs;

    public DvfRowValidator(FileManager logsFileManager) {this.logs = logsFileManager;}

    @Override
    public void validate(Dvf dvf) throws ValidationException {
        validateRequiredFields(dvf);
    }

    private void validateRequiredFields(Dvf dvf) throws ValidationException {
        if (dvf.getIdMutation() == null || dvf.getIdMutation().isBlank()) {
            error("L'id mutation ne peut pas être vide");
        }

        if (dvf.getValeurFonciere() == 0.0) {
            error("La valeur foncière ne peut pas être vide (id mutation : " + dvf.getIdMutation() + ")");
        }

        if (dvf.getAdresseNomVoie() == null || dvf.getAdresseNomVoie().isBlank()) {
            error("Le nom de voie de l'adresse ne peut pas être vide (id mutation : " + dvf.getIdMutation()+ ")");
        }

        if (dvf.getCodePostal() == null) {
            error("Le code postal ne peut pas être vide (id mutation : " + dvf.getIdMutation() + ")");
        }

        if (dvf.getCodeCommune() == null) {
            error("Le code de commune ne peut pas être vide (id mutation : " + dvf.getIdMutation() + ")");
        }

        if (dvf.getNomCommune() == null || dvf.getNomCommune().isBlank()) {
            error("Le nom de la commune ne peut pas être vide (id mutation : " + dvf.getIdMutation() + ")");
        }

        if (dvf.getIdParcelle() == null || dvf.getIdParcelle().isBlank()) {
            error("L'id de la parcelle ne peut pas être vide (id mutation : " + dvf.getIdMutation() + ")");
        }

        if (dvf.getLongitude() == 0.0 || dvf.getLatitude() == 0.0) {
            error("Les coordonnées ne peuvent pas être vide (id mutation : " + dvf.getIdMutation() + ")");
        }
    }

    private void error(String message) throws ValidationException {
        logs.write(message);
        throw new ValidationException(message);
    }
}
