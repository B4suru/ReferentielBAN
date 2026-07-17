package natSystem.BAN.lucene.config.writer;

import lombok.AllArgsConstructor;
import natSystem.BAN.entity.Ban;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.io.IOException;

@AllArgsConstructor
public class LuceneWriter implements ItemWriter<Ban> {
    private final IndexWriter indexWriter;

    @Override
    public void write(Chunk<? extends Ban> chunk) throws IOException {
        for (Ban ban : chunk) {
            indexWriter.addDocument(toDocument(ban));
        }
        indexWriter.commit();
    }


    private Document toDocument(Ban ban) {
        Document doc = new Document();
        doc.add(new StringField("id", ban.getId(), Field.Store.YES));
        doc.add(new StoredField("numero", ban.getNumero() != null ? ban.getNumero() : 0));
        doc.add(new StoredField("rep", ban.getRep() != null ? ban.getRep() : ""));
        doc.add(new StoredField("nomVoie", ban.getNomVoie() != null ? ban.getNomVoie() : ""));
        doc.add(new StoredField("codePostal", ban.getCodePostal() != null ? ban.getCodePostal() : 0));
        doc.add(new StoredField("codeInsee", ban.getCodeInsee() != null ? ban.getCodeInsee() : ""));
        doc.add(new StoredField("nomCommune", ban.getNomCommune() != null ? ban.getNomCommune() : ""));
        doc.add(new StoredField("x", ban.getX()));
        doc.add(new StoredField("y", ban.getY()));
        doc.add(new StoredField("lon", ban.getLon()));
        doc.add(new StoredField("lat", ban.getLat()));

        String full = String.join(" ",
                ban.getNumero() != null ? String.valueOf(ban.getNumero()) : "",
                ban.getRep() != null ? ban.getRep() : "",
                ban.getNomVoie() != null ? ban.getNomVoie() : "",
                ban.getCodePostal() != null ? String.valueOf(ban.getCodePostal()) : "",
                ban.getNomCommune() != null ? ban.getNomCommune() : "");
        doc.add(new TextField("full", full, Field.Store.NO));

        return doc;
    }
}
