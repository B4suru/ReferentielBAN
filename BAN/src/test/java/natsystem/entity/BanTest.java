package natsystem.entity;

import natsystem.ban.entity.Ban;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BanTest {

    private Ban baseBan() {
        Ban ban = new Ban();
        ban.setId("AB123_XY1234_12345");
        ban.setNumero(12);
        ban.setRep("B");
        ban.setNomVoie("Rue de la Paix");
        ban.setCodePostal(75002);
        ban.setCodeInsee("75102");
        ban.setNomCommune("Paris");
        ban.setX(650123.45);
        ban.setY(6862001.23);
        ban.setLon(2.3315);
        ban.setLat(48.8697);
        return ban;
    }

    @Test
    void computeHash_shouldProduceA64CharHexString() {
        Ban ban = baseBan();

        ban.computeHash();
        assertThat(ban.getHash()).matches("^[0-9a-f]{64}$");
    }

    @Test
    void computeHash_shouldMatchExpectedValue_forFixedInput(){
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();

        ban1.computeHash();
        ban2.computeHash();

        assertThat(ban1.getHash()).isEqualTo(ban2.getHash());
    }

    @Test
    void computeHash_shoulNotMatchExpectedValue_forChanchingValue(){
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setNumero(99);

        ban1.computeHash();
        ban2.computeHash();

        assertThat(ban1.getHash()).isNotEqualTo(ban2.getHash());
    }

    @Test
    void compareValue_shouldReturnEmptyString_whenBansAreIdentical() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();

        assertThat(ban1.compareValue(ban2)).isEmpty();
    }

    @Test
    void compareValue_shouldDetectNumeroDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setNumero(99);

        String diff = ban1.compareValue(ban2);

        assertThat(diff).contains("[Numero : 12 | 99]");
    }

    @Test
    void compareValue_shouldDetectRepDifference_includingNull() {
        Ban ban1 = baseBan();
        ban1.setRep(null);
        Ban ban2 = baseBan();
        ban2.setRep("C");

        assertThat(ban1.compareValue(ban2)).contains("[Rep : null | C]");
    }

    @Test
    void compareValue_shouldDetectNomVoieDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setNomVoie("Autre rue");

        assertThat(ban1.compareValue(ban2)).contains("[NomVoie : Rue de la Paix | Autre rue]");
    }

    @Test
    void compareValue_shouldDetectCodePostalDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setCodePostal(75003);

        assertThat(ban1.compareValue(ban2)).contains("[CodePostal : 75002 | 75003]");
    }

    @Test
    void compareValue_shouldDetectCodeInseeDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setCodeInsee("75103");

        assertThat(ban1.compareValue(ban2)).contains("[CodeInsee : 75102 | 75103]");
    }

    @Test
    void compareValue_shouldDetectNomCommuneDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setNomCommune("Lyon");

        assertThat(ban1.compareValue(ban2)).contains("[NomCommune : Paris | Lyon]");
    }

    @Test
    void compareValue_shouldDetectXDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setX(1.0);

        assertThat(ban1.compareValue(ban2)).contains("[X : 650123.45 | 1.0]");
    }

    @Test
    void compareValue_shouldDetectYDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setY(1.0);

        assertThat(ban1.compareValue(ban2)).contains("[Y : 6862001.23 | 1.0]");
    }

    @Test
    void compareValue_shouldDetectLonDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setLon(1.0);

        assertThat(ban1.compareValue(ban2)).contains("[Lon : 2.3315 | 1.0]");
    }

    @Test
    void compareValue_shouldDetectLatDifference() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setLat(1.0);

        assertThat(ban1.compareValue(ban2)).contains("[Lat : 48.8697 | 1.0]");
    }

    @Test
    void compareValue_shouldAccumulateMultipleDifferences() {
        Ban ban1 = baseBan();
        Ban ban2 = baseBan();
        ban2.setNumero(99);
        ban2.setNomCommune("Lyon");

        String diff = ban1.compareValue(ban2);

        assertThat(diff).contains("[Numero : 12 | 99]");
        assertThat(diff).contains("[NomCommune : Paris | Lyon]");
    }
}
