package natSystem.BAN.repository;

import natSystem.BAN.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BanRepository extends JpaRepository<Ban, String> {
    List<Ban> findByCodePostal(int codePostal);

    List<Ban> findByNomVoieContainingIgnoreCase(String nomVoie);

    List<Ban> findByNomCommuneContainingIgnoreCase(String nomCommune);
}
