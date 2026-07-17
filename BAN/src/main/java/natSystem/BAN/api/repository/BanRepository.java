package natSystem.BAN.api.repository;

import natSystem.BAN.entity.Ban;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BanRepository extends JpaRepository<Ban, String> {
    List<Ban> findByCodePostal(int codePostal, Pageable pageable);

    List<Ban> findByNomVoieContainingIgnoreCase(String nomVoie, Pageable pageable);

    List<Ban> findByNomCommuneContainingIgnoreCase(String nomCommune, Pageable pageable);

    @Query("""
        SELECT b FROM Ban b
        WHERE (:codePostal IS NULL OR b.codePostal = :codePostal)
        AND (:nomVoie IS NULL OR LOWER(b.nomVoie) LIKE LOWER(CONCAT('%', :nomVoie, '%')))
        AND (:nomCommune IS NULL OR LOWER(b.nomCommune) LIKE LOWER(CONCAT('%', :nomCommune, '%')))
        AND (:numero IS NULL OR b.numero = :numero)
        AND (:rep IS NULL OR LOWER(b.rep) LIKE LOWER(CONCAT('%', :rep, '%')))
        """)
    List<Ban> rechercher(
            @Param("codePostal") Integer codePostal,
            @Param("nomVoie") String nomVoie,
            @Param("nomCommune") String nomCommune,
            @Param("numero") Integer numero,
            @Param("rep") String rep,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM Ban b
        WHERE b.lat > (:lat) - (:latDiff) AND b.lat < (:lat) + (:latDiff)
        AND b.lon > (:lon) - (:lonDiff) AND b.lon < (:lon) + (:lonDiff)
        """)
    List<Ban>reverseSearch(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("latDiff") double latDiff,
            @Param("lonDiff") double lonDiff
    );
}
