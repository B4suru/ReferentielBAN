package natsystem.dvf.entity;

import java.math.BigDecimal;

public record CommuneTarifDTO(
        String codeCommune,
        BigDecimal  prixMoyen12, BigDecimal  prixMoyen24, BigDecimal  evolPrixMoyen,
        BigDecimal  prixMedian12, BigDecimal  prixMedian24, BigDecimal  evolPrixMedian,
        BigDecimal  prixM212, BigDecimal  prixM224, BigDecimal  evolPrixM2,
        Long nbTransactions12, Long nbTransactions24, BigDecimal evolTransactions
) {
}
