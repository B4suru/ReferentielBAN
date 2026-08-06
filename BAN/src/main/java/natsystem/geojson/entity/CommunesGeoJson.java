package natsystem.geojson.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "communes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CommunesGeoJson {
    @Id
    @Column(name = "code_insee")
    private String codeInsee;

    @Column(name="nom")
    private String nom;

    @Column(name="departement")
    private String departement;

    @Column(name="region")
    private String region;

    @Column(name="epci")
    private String epci;

    @Column(name="geometry")
    private String geometry;
}
