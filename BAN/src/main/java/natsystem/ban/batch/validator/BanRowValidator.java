package natsystem.ban.batch.validator;

import java.util.regex.Pattern;

import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;

import natsystem.ban.entity.Ban;
import natsystem.shared.tools.FileManager;


public class BanRowValidator implements Validator<Ban> {
	private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{5}_[a-zA-Z0-9]{4,6}_\\d{5}(_.+)?$");
	private Ban dernierBan = null;
	private final FileManager logs;

	public BanRowValidator(FileManager logsFileManager) {
		this.logs = logsFileManager;
	}

	@Override
	public void validate(Ban ban) throws ValidationException {
		ban.computeHash();

		validateId(ban);
		validateDuplicate(ban);
		validateRequiredFields(ban);

		dernierBan = ban;
	}

	private void validateId(Ban ban) throws ValidationException {
		if (ban.getId() == null || ban.getId().isBlank()) {
			error("L'id ne peut pas être vide");
		}

		if (!ID_PATTERN.matcher(ban.getId()).matches()) {
			error("Format d'id invalide : " + ban.getId());
		}
	}

	private void validateDuplicate(Ban ban) throws ValidationException {
		if (dernierBan != null && dernierBan.getId().equals(ban.getId())) {
			if (dernierBan.getHash().equals(ban.getHash())) {
				error("Doublon exact détecté, ligne ignorée (id : " + ban.getId() + ")");
			} else {
				error("Doublon avec valeurs différentes détecté (id : " + ban.getId() + ") " + ban.compareValue(dernierBan));
			}
		}
	}

	private void validateRequiredFields(Ban ban) throws ValidationException {
		if (ban.getNumero() == null) {
			error("Le numero ne peux pas etre inférieur a 0 (id : " + ban.getId()+ ")");
		}

		if (ban.getNomVoie() == null || ban.getNomVoie().isBlank()) {
			error("Le nom de la voie ne peut pas être vide (id : " + ban.getId()+ ")");
		}

		if (ban.getCodePostal() == null) {
			error("Le code postal est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getCodeInsee().isEmpty()) {
			error("Le code insee est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getNomCommune() == null || ban.getNomCommune().isBlank()) {
			error("Le nom de la commune ne peut pas être vide (id : " + ban.getId()+ ")");
		}

		if (ban.getX() == 0.0) {
			error("La position X est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getY() == 0.0) {
			error("La position Y est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getLon() == 0.0){
			error("La longitude est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getLat() == 0.0){
			error("La latitude est obligatoire (id : " + ban.getId()+ ")");
		}
	}

	private void error(String message) throws ValidationException {
		logs.write(message);
		throw new ValidationException(message);
	}
}
