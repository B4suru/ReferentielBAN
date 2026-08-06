package natsystem.dvf.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dvf")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Dvf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_mutation")
    String idMutation;

    @Column(name = "date_mutation")
    LocalDate dateMutation;

    @Column(name = "numero_disposition")
    Integer numeroDisposition;

    @Column(name = "nature_mutation")
    String natureMutation;

    @Column(name = "valeur_fonciere")
    Double valeurFonciere;

    @Column(name = "adresse_numero")
    Integer adresseNumero;

    @Column(name = "adresse_suffixe")
    String adresseSuffixe;

    @Column(name = "adresse_nom_voie")
    String adresseNomVoie;

    @Column(name = "adresse_code_voie")
    String adresseCodeVoie;

    @Column(name = "code_postal")
    Integer codePostal;

    @Column(name = "code_commune")
    String codeCommune;

    @Column(name = "nom_commune")
    String nomCommune;

    @Column(name = "code_departement")
    String codeDepartement;

    @Column(name = "ancien_code_commune")
    String ancienCodeCommune;

    @Column(name = "ancien_nom_commune")
    String ancienNomCommune;

    @Column(name = "id_parcelle")
    String idParcelle;

    @Column(name = "ancien_id_parcelle")
    String ancienIdParcelle;

    @Column(name = "numero_volume")
    String numeroVolume;

    @Column(name = "lot1_numero")
    String lot1Numero;

    @Column(name = "lot1_surface_carrez")
    Double lot1SurfaceCarrez;

    @Column(name = "lot2_numero")
    String lot2Numero;

    @Column(name = "lot2_surface_carrez")
    Double lot2SurfaceCarrez;

    @Column(name = "lot3_numero")
    String lot3Numero;

    @Column(name = "lot3_surface_carrez")
    Double lot3SurfaceCarrez;

    @Column(name = "lot4_numero")
    String lot4Numero;

    @Column(name = "lot4_surface_carrez")
    Double lot4SurfaceCarrez;

    @Column(name = "lot5_numero")
    String lot5Numero;

    @Column(name = "lot5_surface_carrez")
    Double lot5SurfaceCarrez;

    @Column(name = "nombre_lots")
    Integer nombreLots;

    @Column(name = "code_type_local")
    Integer codeTypeLocal;

    @Column(name = "type_local")
    String typeLocal;

    @Column(name = "surface_reelle_bati")
    Integer surfaceReelleBati;

    @Column(name = "nombre_pieces_principales")
    Integer nombrePiecesPrincipales;

    @Column(name = "code_nature_culture")
    String codeNatureCulture;

    @Column(name = "nature_culture")
    String natureCulture;

    @Column(name = "code_nature_culture_speciale")
    String  codeNatureCultureSpeciale;

    @Column(name = "nature_culture_speciale")
    String natureCultureSpeciale;

    @Column(name = "surface_terrain")
    Integer surfaceTerrain;

    @Column(name = "longitude")
    Double longitude;

    @Column(name = "latitude")
    Double latitude;
}
