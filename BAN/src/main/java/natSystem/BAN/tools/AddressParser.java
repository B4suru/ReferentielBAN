package natSystem.BAN.tools;
import natSystem.BAN.entity.Ban;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AddressParser {
    private static final List<String> REPS = Arrays.asList("bis", "ter", "quater");

    private final JdbcTemplate jdbc;

    public AddressParser(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Ban parse(String adresse) {
        Ban adresseParse = new Ban();
        StringBuilder rueCommune = new StringBuilder();
        for (String mot : adresse.trim().split("\\s+")) {
            if (mot.matches("\\d+")) {
                int nombre = Integer.parseInt(mot);
                if (mot.length() == 5 && adresseParse.getCodePostal() == null) {
                    if (existsInBd("code_postal", nombre)) {
                        adresseParse.setCodePostal(nombre);
                    }
                } else if (adresseParse.getNumero() == null) {
                    if (existsInBd("numero", nombre)) {
                        adresseParse.setNumero(nombre);
                    }
                }

            } else if (REPS.contains(mot.toLowerCase()) && adresseParse.getRep() == null) {
                adresseParse.setRep(mot);

            } else {
                rueCommune.append(mot).append(" ");
            }
        }

        if (!rueCommune.toString().trim().isEmpty()) {
            parseRueCommune(rueCommune.toString().trim(), adresseParse);
        }

        return adresseParse;
    }


    private void parseRueCommune(String rueCommune, Ban adresseParse) {
        if (existsInBdString("nom_voie", rueCommune)) {
            adresseParse.setNomVoie(rueCommune);
            return;
        }

        String[] mots = rueCommune.split("\\s+");

        String premierMot = mots[0];
        String sansPremiermot = rueCommune.substring(premierMot.length()).trim();

        if (existsInBdString("nom_commune", premierMot)) {
            adresseParse.setNomCommune(premierMot);
            if (existsInBdString("nom_voie", sansPremiermot)) {
                adresseParse.setNomVoie(sansPremiermot);
                return;
            }
            return;
        }

        String dernierMot = mots[mots.length - 1];
        String sansDernierMot = rueCommune.substring(0, rueCommune.length() - dernierMot.length()).trim();

        if (existsInBdString("nom_commune", dernierMot)) {
            adresseParse.setNomCommune(dernierMot);
            if (existsInBdString("nom_voie", sansDernierMot)) {
                adresseParse.setNomVoie(sansDernierMot);
            }
        }
    }

    private boolean existsInBd(String colonne, Object valeur) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ban WHERE " + colonne + " = ?",
                Integer.class,
                valeur
        );
        return count != null && count > 0;
    }

    private boolean existsInBdString(String colonne, String valeur) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ban WHERE UPPER(" + colonne + ") LIKE UPPER(?)",
                Integer.class,
                "%" + valeur + "%"
        );
        return count != null && count > 0;
    }

}
