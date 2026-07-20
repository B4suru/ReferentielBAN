package natSystem.BAN.lucene.config;

import natSystem.BAN.lucene.AddressAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class LuceneConfig {

    @Value("${lucene.index.path:./lucene-index}")
    private String indexPath;

    @Bean
    public Directory luceneDirectory() throws IOException {
        return FSDirectory.open(Paths.get(indexPath));
    }

    @Bean
    public Analyzer luceneAnalyzer() {
        return new AddressAnalyzer();
    }

    @Bean(destroyMethod = "close")
    public IndexWriter indexWriter(Directory directory, Analyzer analyzer) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory, config);
    }
}
