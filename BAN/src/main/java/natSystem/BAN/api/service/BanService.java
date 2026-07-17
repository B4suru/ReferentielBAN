package natSystem.BAN.api.service;

import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.tools.TimerTool;
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

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;


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

    public Ban reverseSearch(double lat, double lon, Double latDiff, Double lonDiff){
        if (latDiff == null){
            latDiff = 0.00300;
        }

        if (lonDiff == null){
            lonDiff = 0.00300;
        }

        TimerTool timerTool = new TimerTool();

        List<Ban> nearBan = repo.reverseSearch(lat, lon, latDiff, lonDiff);

        Double rangeMin = null;
        Ban rangeMinBan = null;
        for (Ban ban : nearBan) {
            double range = haversine(ban.getLat(), ban.getLon(), lat, lon);
            if (rangeMin ==null || range < rangeMin){
                rangeMin = range;
                rangeMinBan = ban;
            }
        }
        System.out.println(timerTool.showTimer());
        return rangeMinBan;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        return 6371000 * c;
    }

    public List<Ban> freeSearch(String adresse, int nbResult) {
        List<Ban> results = new ArrayList<>();
        if (adresse == null || adresse.isBlank()) {
            return results;
        }

        adresse = adresse.replace('-', ' ');
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

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = searcher.doc(scoreDoc.doc);
                results.add(toBan(document));
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
        ban.setCodeInsee(d.get("codeInsee"));
        ban.setNomCommune(d.get("nomCommune"));
        ban.setX(d.getField("x").numericValue().doubleValue());
        ban.setY(d.getField("y").numericValue().doubleValue());
        ban.setLon(d.getField("lon").numericValue().doubleValue());
        ban.setLat(d.getField("lat").numericValue().doubleValue());
        return ban;
    }
}
