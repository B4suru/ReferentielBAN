package natSystem.BAN.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
	
	

}
