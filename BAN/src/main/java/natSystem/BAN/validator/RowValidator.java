package natSystem.BAN.validator;

import java.util.regex.Pattern;

import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.item.validator.Validator;

import natSystem.BAN.entity.Ban;
import natSystem.BAN.file.File;


public class RowValidator implements Validator<Ban> {
	private static final Pattern ID_PATTERN = Pattern.compile("^\\d{5}_[a-zA-Z0-9]{4,6}_\\d{5}(_.+)?$");

	@Override
	public void validate(Ban ban) throws ValidationException {
		File logs = new File("Logs.txt");
		if (ban.getId() == null || ban.getId().isBlank()) {
			logs.write("L'id ne peut pas être vide");
			throw new ValidationException("L'id ne peut pas être vide");
		}
		if (!ID_PATTERN.matcher(ban.getId()).matches()) {
			logs.write("Format d'id invalide : " + ban.getId());
	        throw new ValidationException("Format d'id invalide : " + ban.getId());
	        
	    }
		
		if (ban.getNumero() < 0) {
			logs.write("Le numero ne peux pas etre inférieur a 0 (id : " + ban.getId()+ ")");
			throw new ValidationException("Le numero ne peux pas etre inférieur a 0 (id : " + ban.getId()+ ")");
		}
		
		if (ban.getNomVoie() == null || ban.getId().isBlank()) {
			logs.write("Le nom de la voie ne peut pas être vide (id : " + ban.getId()+ ")");
			throw new ValidationException("Le nom de la voie ne peut pas être vide (id : " + ban.getId()+ ")");
		}
		
		if (ban.getCodePostal() <= 0) {
			logs.write("Le code postal est obligatoire (id : " + ban.getId()+ ")");
            throw new ValidationException("Le code postal est obligatoire (id : " + ban.getId()+ ")");
        }
		
		if (ban.getCodeInsee() <= 0) {
			logs.write("Le code insee est obligatoire (id : " + ban.getId()+ ")");
            throw new ValidationException("Le code insee est obligatoire (id : " + ban.getId()+ ")");
        }
		
		if (ban.getNomCommune() == null || ban.getNomCommune().isBlank()) {
			logs.write("Le nom de la commune ne peut pas être vide (id : " + ban.getId()+ ")");
			throw new ValidationException("Le nom de la commune ne peut pas être vide (id : " + ban.getId()+ ")");
		}
		
		if (ban.getX() <= 0) {
			logs.write("La position X est obligatoire (id : " + ban.getId()+ ")");
			throw new ValidationException("La position X est obligatoire (id : " + ban.getId()+ ")");
		}
		
		if (ban.getY() <= 0) {
			logs.write("La position Y est obligatoire (id : " + ban.getId()+ ")");
			throw new ValidationException("La position Y est obligatoire (id : " + ban.getId()+ ")");
		}
	}

}
