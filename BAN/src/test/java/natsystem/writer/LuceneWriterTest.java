package natsystem.writer;

import natsystem.ban.entity.Ban;
import natsystem.ban.lucene.config.writer.LuceneWriter;
import natsystem.shared.enumeration.Operation;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LuceneWriterTest {
    @Mock
    private IndexWriter indexWriter;

    private LuceneWriter writer;

    @BeforeEach
    void setUp() {
        writer = new LuceneWriter(indexWriter);
    }

    @Test
    void write_shouldAddDocument_whenOperationIsInsert() throws IOException {
        Ban ban = createBan();
        ban.setOperation(Operation.INSERT);

        Chunk<Ban> chunk = new Chunk<>(List.of(ban));

        writer.write(chunk);

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(indexWriter).addDocument(captor.capture());
        verify(indexWriter, never()).updateDocument(any(), any());

        Document document = captor.getValue();

        assertDocument(document, ban);
    }

    @Test
    void write_shouldUpdateDocument_whenOperationIsUpdate() throws IOException {
        Ban ban = createBan();
        ban.setOperation(Operation.UPDATE);

        Chunk<Ban> chunk = new Chunk<>(List.of(ban));

        writer.write(chunk);

        ArgumentCaptor<Term> termCaptor =
                ArgumentCaptor.forClass(Term.class);

        ArgumentCaptor<Document> documentCaptor =
                ArgumentCaptor.forClass(Document.class);

        verify(indexWriter).updateDocument(
                termCaptor.capture(),
                documentCaptor.capture()
        );

        verify(indexWriter, never()).addDocument(any());

        Term term = termCaptor.getValue();

        assertEquals("id", term.field());
        assertEquals(ban.getId(), term.text());

        Document document = documentCaptor.getValue();

        assertDocument(document, ban);
    }

    @Test
    void write_shouldHandleMultipleBans() throws IOException {
        Ban insertBan = createBan();
        insertBan.setId("id-1");
        insertBan.setOperation(Operation.INSERT);

        Ban updateBan = createBan();
        updateBan.setId("id-2");
        updateBan.setOperation(Operation.UPDATE);

        Chunk<Ban> chunk = new Chunk<>(
                List.of(insertBan, updateBan)
        );

        writer.write(chunk);

        verify(indexWriter, times(1)).addDocument(any(Document.class));
        verify(indexWriter, times(1))
                .updateDocument(any(Term.class), any(Document.class));
    }

    @Test
    void write_shouldHandleNullValues() throws IOException {
        Ban ban = new Ban();

        ban.setId("id-null");
        ban.setOperation(Operation.INSERT);

        ban.setNumero(null);
        ban.setRep(null);
        ban.setNomVoie(null);
        ban.setCodePostal(null);
        ban.setCodeInsee(null);
        ban.setNomCommune(null);

        Chunk<Ban> chunk = new Chunk<>(List.of(ban));

        writer.write(chunk);

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(indexWriter).addDocument(captor.capture());

        Document document = captor.getValue();

        assertEquals("id-null", document.get("id"));

        assertEquals("0", document.get("numero"));
        assertEquals("", document.get("rep"));
        assertEquals("", document.get("nomVoie"));
        assertEquals("0", document.get("codePostal"));
        assertEquals("", document.get("codeInsee"));
        assertEquals("", document.get("nomCommune"));
    }

    @Test
    void write_shouldCreateLocationFields() throws IOException {
        Ban ban = createBan();
        ban.setOperation(Operation.INSERT);

        Chunk<Ban> chunk = new Chunk<>(List.of(ban));

        writer.write(chunk);

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(indexWriter).addDocument(captor.capture());

        Document document = captor.getValue();

        assertNotNull(document.getField("location"));
        assertEquals(2, document.getFields("location").length);
    }


    @Test
    void write_shouldPropagateIOException_whenAddDocumentFails()
            throws IOException {
        Ban ban = createBan();
        ban.setOperation(Operation.INSERT);

        Chunk<Ban> chunk = new Chunk<>(List.of(ban));

        doThrow(new IOException("Erreur Lucene"))
                .when(indexWriter)
                .addDocument(any(Document.class));

        IOException exception = assertThrows(
                IOException.class,
                () -> writer.write(chunk)
        );

        assertEquals("Erreur Lucene", exception.getMessage());

        verify(indexWriter).addDocument(any(Document.class));
    }

    @Test
    void write_shouldPropagateIOException_whenUpdateDocumentFails()
            throws IOException {
        Ban ban = createBan();
        ban.setOperation(Operation.UPDATE);

        Chunk<Ban> chunk = new Chunk<>(List.of(ban));

        doThrow(new IOException("Erreur update Lucene"))
                .when(indexWriter)
                .updateDocument(
                        any(Term.class),
                        any(Document.class)
                );

        IOException exception = assertThrows(
                IOException.class,
                () -> writer.write(chunk)
        );

        assertEquals(
                "Erreur update Lucene",
                exception.getMessage()
        );

        verify(indexWriter).updateDocument(
                any(Term.class),
                any(Document.class)
        );
    }


    private Ban createBan() {
        Ban ban = new Ban();

        ban.setId("123456");

        ban.setNumero(12);
        ban.setRep("bis");
        ban.setNomVoie("Rue de Paris");
        ban.setCodePostal(75001);
        ban.setCodeInsee("75056");
        ban.setNomCommune("Paris");

        ban.setX(652345.12);
        ban.setY(6865432.45);

        ban.setLon(2.3522);
        ban.setLat(48.8566);

        return ban;
    }

    private void assertDocument(Document document, Ban ban) {
        assertEquals(ban.getId(), document.get("id"));

        assertEquals(
                String.valueOf(ban.getNumero()),
                document.get("numero")
        );

        assertEquals(
                ban.getRep(),
                document.get("rep")
        );

        assertEquals(
                ban.getNomVoie(),
                document.get("nomVoie")
        );

        assertEquals(
                String.valueOf(ban.getCodePostal()),
                document.get("codePostal")
        );

        assertEquals(
                ban.getCodeInsee(),
                document.get("codeInsee")
        );

        assertEquals(
                ban.getNomCommune(),
                document.get("nomCommune")
        );

        assertEquals(
                String.valueOf(ban.getX()),
                document.get("x")
        );

        assertEquals(
                String.valueOf(ban.getY()),
                document.get("y")
        );

        assertEquals(
                String.valueOf(ban.getLon()),
                document.get("lon")
        );

        assertEquals(
                String.valueOf(ban.getLat()),
                document.get("lat")
        );
    }
}
