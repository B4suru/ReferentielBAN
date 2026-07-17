package natSystem.BAN.batch.context;

import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@StepScope
@Getter
public class BanDiffContext {

    private final Set<String> bdIds;

    public BanDiffContext(JdbcTemplate jdbc, @Value("#{stepExecutionContext['departement']}") String departement) {
        bdIds = new HashSet<>(
                jdbc.queryForList("SELECT id FROM ban WHERE code_insee LIKE ?", new Object[]{departement + "%"}, String.class)
        );
    }

    public Set<String> getBdIds() {
        return bdIds;
    }
}
