package natsystem.service;

import natsystem.api.service.BanService;
import natsystem.ban.entity.Ban;
import natsystem.api.repository.BanRepository;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BanServiceTest {

    @Mock
    private BanRepository repo;

    private final Pageable pageable = PageRequest.of(0, 20);

    // Directory/Analyzer "vides", jamais sollicités par les méthodes qui ne passent pas
    // par Lucene (recherches déléguées au repo, reverseSearch).
    private BanService serviceWithoutLucene() {
        return new BanService(repo, new ByteBuffersDirectory(), new StandardAnalyzer());
    }

    private Ban ban(String id, double lat, double lon) {
        Ban b = new Ban();
        b.setId(id);
        b.setNumero(1);
        b.setNomVoie("Rue Test");
        b.setCodePostal(75002);
        b.setCodeInsee("75102");
        b.setNomCommune("Paris");
        b.setX(0);
        b.setY(0);
        b.setLat(lat);
        b.setLon(lon);
        return b;
    }

    @Test
    void searchByCodePostal_shouldDelegateToRepository() {
        List<Ban> expected = List.of(ban("A", 48.86, 2.35));
        when(repo.findByCodePostal(75002, pageable)).thenReturn(expected);

        List<Ban> result = serviceWithoutLucene().searchByCodePostal(75002, pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void searchByRue_shouldDelegateToRepository() {
        List<Ban> expected = List.of(ban("A", 48.86, 2.35));
        when(repo.findByNomVoieContainingIgnoreCase("paix", pageable)).thenReturn(expected);

        List<Ban> result = serviceWithoutLucene().searchByRue("paix", pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void searchByCommune_shouldDelegateToRepository() {
        List<Ban> expected = List.of(ban("A", 48.86, 2.35));
        when(repo.findByNomCommuneContainingIgnoreCase("paris", pageable)).thenReturn(expected);

        List<Ban> result = serviceWithoutLucene().searchByCommune("paris", pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void search_shouldDelegateToRepository_withAllCriteria() {
        List<Ban> expected = List.of(ban("A", 48.86, 2.35));
        when(repo.rechercher(75002, "paix", "paris", 12, "B", pageable)).thenReturn(expected);

        List<Ban> result = serviceWithoutLucene().search(75002, "paix", "paris", 12, "B", pageable);

        assertThat(result).isEqualTo(expected);
        verify(repo).rechercher(75002, "paix", "paris", 12, "B", pageable);
    }

    // ---- freeSearch (recherche plein texte Lucene réelle) ----

    private void addBanDocument(IndexWriter writer, String id, String fullText, String nomVoie,
                                String nomCommune, int codePostal) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", id, Field.Store.YES));
        doc.add(new TextField("full", fullText, Field.Store.NO));
        doc.add(new StoredField("numero", 12));
        doc.add(new StoredField("rep", "")); // StoredField(String,String) : jamais null en pratique
        doc.add(new StoredField("nomVoie", nomVoie));
        doc.add(new StoredField("codePostal", codePostal));
        doc.add(new StoredField("codeInsee", "75102"));
        doc.add(new StoredField("nomCommune", nomCommune));
        doc.add(new StoredField("x", 650000.0));
        doc.add(new StoredField("y", 6862000.0));
        doc.add(new StoredField("lon", 2.33));
        doc.add(new StoredField("lat", 48.86));
        writer.addDocument(doc);
    }

    @Test
    void freeSearch_shouldReturnEmptyList_whenAdresseIsNullOrBlank() {
        BanService service = serviceWithoutLucene();

        assertThat(service.freeSearch(null, 10)).isEmpty();
        assertThat(service.freeSearch("   ", 10)).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    void freeSearch_shouldFindMatchingDocument() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                addBanDocument(writer, "paix", "rue de la paix paris", "Rue de la Paix", "Paris", 75002);
                addBanDocument(writer, "elysees", "avenue des champs elysees paris", "Avenue des Champs Elysées", "Paris", 75008);
            }

            BanService service = new BanService(repo, directory, analyzer);

            List<Ban> results = service.freeSearch("Rue de la Paix", 10);

            assertThat(results).extracting(Ban::getId).contains("paix");
        }
    }

    @Test
    void freeSearch_shouldReplaceHyphensWithSpaces() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                addBanDocument(writer, "paix", "rue de la paix paris", "Rue de la Paix", "Paris", 75002);
            }

            BanService service = new BanService(repo, directory, analyzer);

            List<Ban> results = service.freeSearch("Rue-de-la-Paix", 10);

            assertThat(results).extracting(Ban::getId).contains("paix");
        }
    }

    @Test
    void freeSearch_shouldRespectNbResultLimit() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                addBanDocument(writer, "a", "rue paris commune", "Rue A", "Paris", 75001);
                addBanDocument(writer, "b", "rue paris commune", "Rue B", "Paris", 75002);
                addBanDocument(writer, "c", "rue paris commune", "Rue C", "Paris", 75003);
            }

            BanService service = new BanService(repo, directory, analyzer);

            List<Ban> results = service.freeSearch("rue paris commune", 1);

            assertThat(results).hasSize(1);
        }
    }

    @Test
    void freeSearch_shouldReturnEmptyList_whenNothingMatches() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                addBanDocument(writer, "paix", "rue de la paix paris", "Rue de la Paix", "Paris", 75002);
            }

            BanService service = new BanService(repo, directory, analyzer);

            List<Ban> results = service.freeSearch("boulevard totalement different xyzabc", 10);

            assertThat(results).isEmpty();
        }
    }

    // ---- reverseSearchLucene (LatLonPoint réel) ----

    private void addGeoBanDocument(IndexWriter writer, String id, double lat, double lon) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", id, Field.Store.YES));
        doc.add(new LatLonPoint("location", lat, lon));
        doc.add(new LatLonDocValuesField("location", lat, lon));
        doc.add(new StoredField("numero", 12));
        doc.add(new StoredField("rep", ""));
        doc.add(new StoredField("nomVoie", "Rue Test"));
        doc.add(new StoredField("codePostal", 75002));
        doc.add(new StoredField("codeInsee", "75102"));
        doc.add(new StoredField("nomCommune", "Paris"));
        doc.add(new StoredField("x", 650000.0));
        doc.add(new StoredField("y", 6862000.0));
        doc.add(new StoredField("lon", lon));
        doc.add(new StoredField("lat", lat));
        writer.addDocument(doc);
    }

    @Test
    void reverseSearchLucene_shouldReturnNull_whenNoDocumentWithinRadius() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                // Lyon, très loin du point recherché (Paris) et hors du rayon demandé
                addGeoBanDocument(writer, "lyon", 45.7640, 4.8357);
            }

            BanService service = new BanService(repo, directory, analyzer);

            Ban result = service.reverseSearchLucene(48.8566, 2.3522, 1000); // 1 km

            assertThat(result).isNull();
        }
    }

    @Test
    void reverseSearchLucene_shouldReturnClosestDocument_withDistanceSet() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                addGeoBanDocument(writer, "loin", 48.9000, 2.4000);  // ~ quelques km
                addGeoBanDocument(writer, "proche", 48.8567, 2.3523); // ~ quelques mètres
            }

            BanService service = new BanService(repo, directory, analyzer);

            Ban result = service.reverseSearchLucene(48.8566, 2.3522, 50000); // 50 km

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("proche");
            assertThat(result.getDistance()).isGreaterThan(0);
        }
    }

    @Test
    void reverseSearchLucene_shouldMapAllFieldsCorrectly() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        try (Directory directory = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                addGeoBanDocument(writer, "test-id", 48.8566, 2.3522);
            }

            BanService service = new BanService(repo, directory, analyzer);

            Ban result = service.reverseSearchLucene(48.8566, 2.3522, 1000);

            assertThat(result.getId()).isEqualTo("test-id");
            assertThat(result.getNumero()).isEqualTo(12);
            assertThat(result.getCodePostal()).isEqualTo(75002);
            assertThat(result.getCodeInsee()).isEqualTo("75102");
            assertThat(result.getNomCommune()).isEqualTo("Paris");
            assertThat(result.getLat()).isEqualTo(48.8566);
            assertThat(result.getLon()).isEqualTo(2.3522);
        }
    }
}
