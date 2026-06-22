package natSystem.BAN.batchConfig;

import javax.sql.DataSource;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import natSystem.BAN.entity.Ban;

@Configuration
public class banBatchConfig {
	@Bean
	public Job banBatchJob(JobRepository jobRepository, Step banStep1) {
		return new JobBuilder("banBatchJob", jobRepository)
				.start(banStep1)
				.build();
	}
	
	@Bean
	public Step banStep1(JobRepository jobRepository, PlatformTransactionManager txtManager,
			FlatFileItemReader<Ban> csvReader,
			ItemWriter<Ban> writer) {
		return new StepBuilder("banStep1", jobRepository)
				.<Ban, Ban>chunk(100)
				.transactionManager(txtManager)
				.reader(csvReader)
				.writer(writer)
				.build();
		
	}
	
	@Bean
	public FlatFileItemReader<Ban> csvReader() {
		return new FlatFileItemReaderBuilder<Ban>()
			 .name("banCsvReader")
			 .resource(new ClassPathResource("adresses-with-ids-79.csv"))
			 .delimited()
			 .delimiter(";")
			 .names("id",
	                    "id_fantoir",
	                    "numero",
	                    "rep",
	                    "nom_voie",
	                    "code_postal",
	                    "code_insee",
	                    "nom_commune",
	                    "code_insee_ancienne_commune",
	                    "nom_ancienne_commune",
	                    "x",
	                    "y",
	                    "lon",
	                    "lat",
	                    "type_position",
	                    "alias",
	                    "nom_ld",
	                    "libelle_acheminement",
	                    "nom_afnor",
	                    "source_position",
	                    "source_nom_voie",
	                    "certification_commune",
	                    "cad_parcelles",
	                    "id_ban_adresse",
	                    "id_ban_toponyme",
	                    "id_ban_commune")
			 .fieldSetMapper(fs -> {
				    Ban b = new Ban();

				    b.setId(fs.readString("id"));
				    b.setNumero(fs.readInt("numero"));
				    b.setRep(fs.readString("rep"));
				    b.setNomVoie(fs.readString("nom_voie"));
				    b.setCodePostal(fs.readInt("code_postal"));
				    b.setCodeInsee(fs.readInt("code_insee"));
				    b.setNomCommune(fs.readString("nom_commune"));
				    b.setX(fs.readLong("x"));
				    b.setY(fs.readLong("y"));
				    b.setLon(fs.readDouble("lon"));
				    b.setLat(fs.readDouble("lat"));
				    return b;})
			 .linesToSkip(1) // ignorer l'en-tête
			 .build();
	}

	@Bean
	public JdbcBatchItemWriter<Ban> jdbcWriter(DataSource ds) {
	 return new JdbcBatchItemWriterBuilder<Ban>()
	 .dataSource(ds)
	 .sql("""
	 INSERT INTO ban (
    id,
    numero,
    rep,
    nom_voie,
    code_postal,
    code_insee,
    nom_commune,
    x,
    y,
    lon,
    lat
)
VALUES (
    :id,
    :numero,
    :rep,
    :nomVoie,
    :codePostal,
    :codeInsee,
    :nomCommune,
    :x,
    :y,
    :lon,
    :lat
)
ON CONFLICT(id) DO UPDATE SET
    numero = excluded.numero,
    rep = excluded.rep,
    nom_voie = excluded.nom_voie,
    code_postal = excluded.code_postal,
    code_insee = excluded.code_insee,
    nom_commune = excluded.nom_commune,
    x = excluded.x,
    y = excluded.y,
    lon = excluded.lon,
    lat = excluded.lat;
	 """)
	 .beanMapped()
	 .build();
	}

}
