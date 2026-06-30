package natSystem.BAN.batch.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;

import natSystem.BAN.entity.Ban;
import natSystem.BAN.tools.FileManager;


public class RowValidator implements Validator<Ban> {
	private static final Pattern ID_PATTERN = Pattern.compile("^\\d{5}_[a-zA-Z0-9]{4,6}_\\d{5}(_.+)?$");
	private Ban dernierBan = null;
	private final FileManager logs;

	public RowValidator(String logFileName) {
		this.logs = new FileManager(logFileName);
	}

	@Override
	public void validate(Ban ban) throws ValidationException {
		if (ban.getId() == null || ban.getId().isBlank()) {
			this.logs.write("L'id ne peut pas être vide");
			throw new ValidationException("L'id ne peut pas être vide");
		}
		
		if (!ID_PATTERN.matcher(ban.getId()).matches()) {
			this.logs.write("Format d'id invalide : " + ban.getId());
	        throw new ValidationException("Format d'id invalide : " + ban.getId());
	    }

		if (dernierBan != null && dernierBan.getId().equals(ban.getId())) {
			if (dernierBan.equals(ban)) {
				this.logs.write("Doublon exact détecté, ligne ignorée (id : " + ban.getId() + ")");
                throw new ValidationException("Doublon exact détecté (id : " + ban.getId() + ")");
            } else {
				this.logs.write("Doublon avec valeurs différentes détecté (id : " + ban.getId() + ") " + ban.compareValue(dernierBan));
				throw new ValidationException("Doublon avec conflit de valeurs détecté (id : " + ban.getId() + ")");
            }
        }
		dernierBan = ban;

		if (ban.getNumero() == null) {
			this.logs.write("Le numero ne peux pas etre inférieur a 0 (id : " + ban.getId()+ ")");
			throw new ValidationException("Le numero ne peux pas etre inférieur a 0 (id : " + ban.getId()+ ")");
		}
		
		if (ban.getNomVoie() == null || ban.getNomVoie().isBlank()) {
			this.logs.write("Le nom de la voie ne peut pas être vide (id : " + ban.getId()+ ")");
			throw new ValidationException("Le nom de la voie ne peut pas être vide (id : " + ban.getId()+ ")");
		}

		if (ban.getCodePostal() == null) {
			this.logs.write("Le code postal est obligatoire (id : " + ban.getId()+ ")");
            throw new ValidationException("Le code postal est obligatoire (id : " + ban.getId()+ ")");
        }

		if (ban.getCodeInsee() == null) {
			this.logs.write("Le code insee est obligatoire (id : " + ban.getId()+ ")");
            throw new ValidationException("Le code insee est obligatoire (id : " + ban.getId()+ ")");
        }
		
		if (ban.getNomCommune() == null || ban.getNomCommune().isBlank()) {
			this.logs.write("Le nom de la commune ne peut pas être vide (id : " + ban.getId()+ ")");
			throw new ValidationException("Le nom de la commune ne peut pas être vide (id : " + ban.getId()+ ")");
		}
		
		if (ban.getX() == 0.0) {
			this.logs.write("La position X est obligatoire (id : " + ban.getId()+ ")");
			throw new ValidationException("La position X est obligatoire (id : " + ban.getId()+ ")");
		}
		
		if (ban.getY() == 0.0) {
			this.logs.write("La position Y est obligatoire (id : " + ban.getId()+ ")");
			throw new ValidationException("La position Y est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getLon() == 0.0){
			this.logs.write("La longitude est obligatoire (id : " + ban.getId()+ ")");
			throw new ValidationException("La longitude est obligatoire (id : " + ban.getId()+ ")");
		}

		if (ban.getLat() == 0.0){
			this.logs.write("La latitude est obligatoire (id : " + ban.getId()+ ")");
			throw new ValidationException("La latitude est obligatoire (id : " + ban.getId()+ ")");
		}
	}
}
