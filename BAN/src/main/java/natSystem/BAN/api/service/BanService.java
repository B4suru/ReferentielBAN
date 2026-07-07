package natSystem.BAN.api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Query;
import lombok.AllArgsConstructor;
import natSystem.BAN.entity.Ban;
import natSystem.BAN.api.repository.BanRepository;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class BanService {
    private final BanRepository repo;
    private final Directory directory;
    private final Analyzer analyzer;

    public List<Ban> searchByCodePostal(int cp, Pageable pageable) {
        return repo.findByCodePostal(cp, pageable);
    }

    public List<Ban> searchByRue(String rue, Pageable pageable) {
        return repo.findByNomVoieContainingIgnoreCase(rue, pageable);
    }

    public List<Ban> searchByCommune(String commune, Pageable pageable) {
        return repo.findByNomCommuneContainingIgnoreCase(commune, pageable);
    }

    public List<Ban> search(Integer cp, String rue, String commune, Integer numero, String rep, Pageable pageable) {
        return repo.rechercher(cp, rue,commune, numero, rep, pageable);
    }

    public List<Ban> freeSearch(String adresse, int nbResult) {
        System.out.println("Analyzer utilisé : " + analyzer.getClass().getName());
        List<Ban> results = new ArrayList<>();
        if (adresse == null || adresse.isBlank()) {
            return results;
        }

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser("full", analyzer);
            parser.setDefaultOperator(QueryParser.Operator.AND);

            String queryStr  = Arrays.stream(adresse.trim().split("\\s+"))
                    .map(t -> QueryParser.escape(t) + "~")
                    .collect(Collectors.joining(" ")).toLowerCase();

            System.out.println("Requette : " + queryStr);

            Query query = parser.parse(queryStr );
            TopDocs topDocs = searcher.search(query, nbResult);

            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document d = searcher.doc(sd.doc);
                results.add(toBan(d));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche", e);
        }
        return results;
    }

    private Ban toBan(Document d) {
        Ban ban = new Ban();
        ban.setId(d.get("id"));
        ban.setNumero(d.getField("numero").numericValue().intValue());
        ban.setRep(d.get("rep"));
        ban.setNomVoie(d.get("nomVoie"));
        ban.setCodePostal(d.getField("codePostal").numericValue().intValue());
        ban.setCodeInsee(d.getField("codeInsee").numericValue().intValue());
        ban.setNomCommune(d.get("nomCommune"));
        ban.setX(d.getField("x").numericValue().doubleValue());
        ban.setY(d.getField("y").numericValue().doubleValue());
        ban.setLon(d.getField("lon").numericValue().doubleValue());
        ban.setLat(d.getField("lat").numericValue().doubleValue());
        return ban;
    }
}
