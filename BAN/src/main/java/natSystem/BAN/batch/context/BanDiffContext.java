package natSystem.BAN.batch.context;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@StepScope
public class BanDiffContext {

    private final Set<String> bdIds;

    public BanDiffContext(JdbcTemplate jdbc) {
        bdIds = new HashSet<>(
                jdbc.queryForList("SELECT id FROM ban", String.class)
        );
    }

    public Set<String> getBdIds() {
        return bdIds;
    }
}
