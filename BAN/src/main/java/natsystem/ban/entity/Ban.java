package natsystem.ban.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.*;
import natsystem.shared.enumeration.Operation;

@Entity
@Table(name = "ban")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Ban {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "numero")
    private Integer numero;

	@Column(name = "rep")
    private String rep;

	@Column(name = "nom_voie")
    private String nomVoie;

	@Column(name = "code_postal")
    private Integer codePostal;

	@Column(name = "code_insee")
    private String codeInsee;

	@Column(name = "nom_commune")
    private String nomCommune;
    
	@Column(name = "x")
    private double x;
    
	@Column(name = "y")
    private double y;

	@Column(name = "lon")
    private double lon;
    
	@Column(name = "lat")
    private double lat;

	@Column(name= "hash")
	private String hash;

	private double distance;

	@Transient //Non persistant
	private Operation operation;

	public void computeHash() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			String data = String.join("|",
					String.valueOf(id),
					String.valueOf(numero),
					String.valueOf(rep),
					String.valueOf(nomVoie),
					String.valueOf(codePostal),
					String.valueOf(codeInsee),
					String.valueOf(nomCommune),
					String.valueOf(x),
					String.valueOf(y),
					String.valueOf(lon),
					String.valueOf(lat)
			);

			byte[] hashBytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
			hash = HexFormat.of().formatHex(hashBytes);

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

	}

	public String compareValue(Ban other) {
		String diff = "";
		if (!numero.equals(other.getNumero())) {
			diff += "[Numero : " + numero + " | " + other.getNumero() + "] ";
		}
		if (!Objects.equals(rep, other.getRep())) {
			diff += "[Rep : " + rep + " | " + other.getRep() + "] ";
		}
		if (!Objects.equals(nomVoie, other.getNomVoie())) {
			diff += "[NomVoie : " + nomVoie + " | " + other.getNomVoie() + "] ";
		}
		if (!codePostal.equals(other.getCodePostal())) {
			diff += "[CodePostal : " + codePostal + " | " + other.getCodePostal() + "] ";
		}
		if (!codeInsee.equals(other.getCodeInsee())) {
			diff += "[CodeInsee : " + codeInsee + " | " + other.getCodeInsee() + "] ";
		}
		if (!Objects.equals(nomCommune, other.getNomCommune())) {
			diff += "[NomCommune : " + nomCommune + " | " + other.getNomCommune() + "] ";
		}
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.getX())) {
			diff += "[X : " + x + " | " + other.getX() + "] ";
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.getY())) {
			diff += "[Y : " + y + " | " + other.getY() + "] ";
		}
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)) {
			diff += "[Lon : " + lon + " | " + other.getLon() + "] ";
		}
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) {
			diff += "[Lat : " + lat + " | " + other.getLat() + "] ";
		}
		return diff;
	}
}
