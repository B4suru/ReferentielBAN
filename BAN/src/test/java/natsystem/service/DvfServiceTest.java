package natsystem.service;

import natsystem.api.repository.DvfRepository;
import natsystem.api.service.DvfService;
import natsystem.dvf.entity.CommuneTarifDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DvfServiceTest {
    @Mock
    private DvfRepository repo;

    @InjectMocks
    private DvfService dvfService;

    private CommuneTarifDTO communeTarifDTO(String codeCommune) {
        return new CommuneTarifDTO(
                codeCommune,
                new BigDecimal("450000.00"),
                new BigDecimal("430000.00"),
                new BigDecimal("4.65"),
                new BigDecimal("400000.00"),
                new BigDecimal("390000.00"),
                new BigDecimal("2.56"),
                new BigDecimal("9500.00"),
                new BigDecimal("9000.00"),
                new BigDecimal("5.56"),
                120L,
                110L,
                new BigDecimal("9.09")
        );
    }

    @Test
    void tarif_shouldReturnRepositoryResult() {
        // Arrange
        String codeCommune = "75056";
        CommuneTarifDTO expected = communeTarifDTO( codeCommune);


        when(repo.tarif(codeCommune)).thenReturn(expected);

        // Act
        CommuneTarifDTO result = dvfService.tarif(codeCommune);

        // Assert
        assertSame(expected, result);
        verify(repo).tarif(codeCommune);
    }
}
