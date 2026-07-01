package natSystem.BAN.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
    private Integer codeInsee;

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ban other = (Ban) obj;
		return codeInsee.equals(other.codeInsee)  && codePostal.equals(other.codePostal) && Objects.equals(id, other.id)
				&& Double.doubleToLongBits(lat) == Double.doubleToLongBits(other.lat)
				&& Double.doubleToLongBits(lon) == Double.doubleToLongBits(other.lon)
				&& Objects.equals(nomCommune, other.nomCommune) && Objects.equals(nomVoie, other.nomVoie)
				&& numero.equals(other.numero) && Objects.equals(rep, other.rep)
				&& Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
				&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
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
