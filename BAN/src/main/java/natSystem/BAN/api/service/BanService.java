package natSystem.BAN.api.service;

import lombok.AllArgsConstructor;
import natSystem.BAN.entity.Ban;
import natSystem.BAN.api.repository.BanRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class BanService {
    private final BanRepository repo;

    public List<Ban> searchByCodePostal(int cp) {
        return repo.findByCodePostal(cp);
    }

    public List<Ban> searchByRue(String rue) {
        return repo.findByNomVoieContainingIgnoreCase(rue);
    }

    public List<Ban> searchByCommune(String commune) {
        return repo.findByNomCommuneContainingIgnoreCase(commune);
    }

    public List<Ban> search(Integer cp, String rue, String commune) {
        return repo.rechercher(cp, rue,commune);
    }
}
