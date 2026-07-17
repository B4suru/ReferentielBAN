package natSystem.BAN;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.batch.BatchInitialization;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@OpenAPIDefinition(
		info = @Info(
				title = "Ban api",
				version = "1.0",
				description = "API documentation pour référentiel BAN"
		)
)
@SpringBootApplication
public class BanApplication {
	public static void main(String[] args) {
		SpringApplication.run(BanApplication.class, args);
	}
}
