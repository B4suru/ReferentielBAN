package natsystem.api.repository;

import natsystem.dvf.entity.CommuneTarifDTO;
import natsystem.dvf.entity.Dvf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DvfRepository extends JpaRepository<Dvf, String> {
    @Query(value = """
    WITH stats12 AS (
        SELECT
            code_commune,
            ROUND(AVG(valeur_fonciere)::numeric, 2) AS prix_moyen_12,
            ROUND(PERCENTILE_CONT(0.5)
                WITHIN GROUP (ORDER BY valeur_fonciere)::numeric, 2) AS prix_median_12,
            ROUND(AVG(valeur_fonciere / NULLIF(surface_reelle_bati,0))::numeric, 2) AS prix_m2_12,
            COUNT(*) AS nb_transactions_12

        FROM dvf
        WHERE date_mutation >= CURRENT_DATE - INTERVAL '12 months'\s
            AND type_local IN ('Local industriel. commercial ou assimilé', 'Appartement', 'Maison')
        GROUP BY code_commune
    ),
    stats24 AS (
        SELECT
            code_commune,
            ROUND(AVG(valeur_fonciere)::numeric, 2) AS prix_moyen_24,
            ROUND(PERCENTILE_CONT(0.5)
                WITHIN GROUP (ORDER BY valeur_fonciere)::numeric, 2) AS prix_median_24,
            ROUND(AVG(valeur_fonciere / NULLIF(surface_reelle_bati,0))::numeric, 2) AS prix_m2_24,
            COUNT(*) AS nb_transactions_24

        FROM dvf
        WHERE date_mutation >= CURRENT_DATE - INTERVAL '24 months'
            AND date_mutation <  CURRENT_DATE - INTERVAL '12 months'\s
            AND type_local IN ('Local industriel. commercial ou assimilé', 'Appartement', 'Maison')
        GROUP BY code_commune
    )
    SELECT
        s12.code_commune,
        s12.prix_moyen_12,
        s24.prix_moyen_24,

        ROUND((100 * (s12.prix_moyen_12 - s24.prix_moyen_24)/ NULLIF(s24.prix_moyen_24,0))::numeric,2)\s
        AS evol_prix_moyen,

        s12.prix_median_12,
        s24.prix_median_24,

        ROUND((100 * (s12.prix_median_12 - s24.prix_median_24)/ NULLIF(s24.prix_median_24,0))::numeric,2)\s
        AS evol_prix_median,

        s12.prix_m2_12,
        s24.prix_m2_24,
        ROUND((100 * (s12.prix_m2_12 - s24.prix_m2_24)/ NULLIF(s24.prix_m2_24,0))::numeric,2)\s
        AS evol_prix_m2,

        s12.nb_transactions_12,
        s24.nb_transactions_24,

        ROUND((100 * (s12.nb_transactions_12 - s24.nb_transactions_24)/ NULLIF(s24.nb_transactions_24,0))::numeric,2)\s
        AS evol_transactions
    FROM stats12 s12 JOIN stats24 s24 ON s12.code_commune = s24.code_commune
    WHERE s12.code_commune = :codeCommune;
""", nativeQuery = true)
    CommuneTarifDTO tarif(@Param("codeCommune") String codeCommune);
}
