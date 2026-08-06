package natsystem.shared.config;

import natsystem.shared.tools.FileManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BanFileManagerConfig {
    @Bean
    //@JobScope
    public FileManager rapportFileManager() {
        return new FileManager();
    }

    @Bean
    //@JobScope
    public FileManager logsFileManager() {
        return new FileManager();
    }
}
