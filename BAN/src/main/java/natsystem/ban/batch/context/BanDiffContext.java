package natsystem.ban.batch.context;

import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@StepScope
@Getter
public class BanDiffContext {
    private final Map<String, String> bdMap;
    private final AtomicInteger insertCount = new AtomicInteger();
    private final AtomicInteger updateCount = new AtomicInteger();


    public BanDiffContext(JdbcTemplate jdbc, @Value("#{stepExecutionContext['departement']}") String departement) {
        bdMap = jdbc.query(
            "SELECT id, hash FROM ban WHERE code_insee LIKE ?",
            ps -> ps.setString(1, departement + "%"),
            rs -> {
                Map<String, String> map = new HashMap<>();
                while (rs.next()) {
                    map.put(rs.getString("id"), rs.getString("hash"));
                }
                return map;
            }
        );
    }
}
