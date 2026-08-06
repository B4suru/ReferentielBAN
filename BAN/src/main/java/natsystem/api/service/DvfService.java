package natsystem.api.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import natsystem.dvf.entity.CommuneTarifDTO;
import natsystem.api.repository.DvfRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DvfService {
    private final DvfRepository dvfRepository;

    public CommuneTarifDTO tarif(@Param("codeCommune") String codeCommune) {
        return dvfRepository.tarif(codeCommune);
    }

}
