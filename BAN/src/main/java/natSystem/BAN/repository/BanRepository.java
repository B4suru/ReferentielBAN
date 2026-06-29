package natSystem.BAN.repository;

import natSystem.BAN.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BanRepository extends JpaRepository<Ban, String> {
    List<Ban> findByCodePostal(int codePostal);

    List<Ban> findByNomVoieContainingIgnoreCase(String nomVoie);

    List<Ban> findByNomCommuneContainingIgnoreCase(String nomCommune);

    @Query("""
        SELECT b FROM Ban b
        WHERE (:codePostal IS NULL OR b.codePostal = :codePostal)
        AND (:nomVoie IS NULL OR LOWER(b.nomVoie) LIKE LOWER(CONCAT('%', :nomVoie, '%')))
        AND (:nomCommune IS NULL OR LOWER(b.nomCommune) LIKE LOWER(CONCAT('%', :nomCommune, '%')))
        """)
    List<Ban> rechercher(
            @Param("codePostal") Integer codePostal,
            @Param("nomVoie") String nomVoie,
            @Param("nomCommune") String nomCommune
    );
}
