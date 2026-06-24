package natSystem.BAN.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ban")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ban {
	@Id
	@Column(name = "id")
    private String id;
	
	@Column(name = "numero")
    private int numero;
    
	@Column(name = "rep")
    private String rep;
    
	@Column(name = "nom_voie")
    private String nomVoie;
    
	@Column(name = "code_postal")
    private int codePostal;

	@Column(name = "code_insee")
    private int codeInsee;

	@Column(name = "nom_commune")
    private String nomCommune;
    
	@Column(name = "x")
    private long x;
    
	@Column(name = "y")
    private long y;

	@Column(name = "lon")
    private double lon;
    
	@Column(name = "lat")
    private double lat;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public String getRep() {
		return rep;
	}

	public void setRep(String rep) {
		this.rep = rep;
	}

	public String getNomVoie() {
		return nomVoie;
	}

	public void setNomVoie(String nomVoie) {
		this.nomVoie = nomVoie;
	}

	public int getCodePostal() {
		return codePostal;
	}

	public void setCodePostal(int codePostal) {
		this.codePostal = codePostal;
	}

	public int getCodeInsee() {
		return codeInsee;
	}

	public void setCodeInsee(int codeInsee) {
		this.codeInsee = codeInsee;
	}

	public String getNomCommune() {
		return nomCommune;
	}

	public void setNomCommune(String nomCommune) {
		this.nomCommune = nomCommune;
	}

	public long getX() {
		return x;
	}

	public void setX(long x) {
		this.x = x;
	}

	public long getY() {
		return y;
	}

	public void setY(long y) {
		this.y = y;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(codeInsee), Integer.valueOf(codePostal), id, Double.valueOf(lat),
				Double.valueOf(lon), nomCommune, nomVoie, Integer.valueOf(numero), rep, Long.valueOf(x),
				Long.valueOf(y));
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ban other = (Ban) obj;
		return codeInsee == other.codeInsee && codePostal == other.codePostal && Objects.equals(id, other.id)
				&& Double.doubleToLongBits(lat) == Double.doubleToLongBits(other.lat)
				&& Double.doubleToLongBits(lon) == Double.doubleToLongBits(other.lon)
				&& Objects.equals(nomCommune, other.nomCommune) && Objects.equals(nomVoie, other.nomVoie)
				&& numero == other.numero && Objects.equals(rep, other.rep) && x == other.x && y == other.y;
	}
	
	public String compareValue(Ban other) {
		String diff = "";
		if (numero != other.getNumero()) {
			diff += "[Numero : " + numero + " | " + other.getNumero() + "] ";
		}
		if (!Objects.equals(rep, other.getRep())) {
			diff += "[Rep : " + rep + " | " + other.getRep() + "] ";
		}
		if (!Objects.equals(nomVoie, other.getNomVoie())) {
			diff += "[NomVoie : " + nomVoie + " | " + other.getNomVoie() + "] ";
		}
		if (codePostal!= other.getCodePostal()) {
			diff += "[CodePostal : " + codePostal + " | " + other.getCodePostal() + "] ";
		}
		if (codeInsee != other.getCodeInsee()) {
			diff += "[CodeInsee : " + codeInsee + " | " + other.getCodeInsee() + "] ";
		}
		if (!Objects.equals(nomCommune, other.getNomCommune())) {
			diff += "[NomCommune : " + nomCommune + " | " + other.getNomCommune() + "] ";
		}
		if (x != other.getX()) {
			diff += "[X : " + x + " | " + other.getX() + "] ";
		}
		if (y != other.getY()) {
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
