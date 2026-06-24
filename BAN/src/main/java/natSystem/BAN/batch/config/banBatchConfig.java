package natSystem.BAN.batch.config;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.persistence.ExitStatus;
import org.springframework.batch.core.repository.persistence.StepExecution;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import natSystem.BAN.batch.stepListener.BanStepListener;
import natSystem.BAN.batch.validator.RowValidator;
import natSystem.BAN.entity.Ban;


@Configuration
public class banBatchConfig {
	@Bean
	public Job banBatchJob(JobRepository jobRepository, Step banStep) {
		return new JobBuilder("banBatchJob", jobRepository)
				.start(banStep)
				.build();
	}
	
	@Bean
	public Step banStep(JobRepository jobRepository, PlatformTransactionManager txtManager,
			FlatFileItemReader<Ban> csvReader,
			CompositeItemProcessor<Ban, Ban> compositeItemProcessor,
			ItemWriter<Ban> writer,
			BanStepListener listener) {
		return new StepBuilder("banStep", jobRepository)
				.<Ban, Ban>chunk(1000)
				.transactionManager(txtManager)
				.reader(csvReader)
				.processor(compositeItemProcessor)
				.writer(writer)
				.listener(listener)
				.build();
		
	}
	
	@Bean
	public CompositeItemProcessor<Ban, Ban> compositeProcessor(
	        ValidatingItemProcessor<Ban> validatingProcessor,
	        ItemProcessor<Ban, Ban> processor
	) {
	    CompositeItemProcessor<Ban, Ban> composite = new CompositeItemProcessor<>();
	    composite.setDelegates(Arrays.asList(validatingProcessor, processor));
	    return composite;
	}
	
	@Bean
	@StepScope
	public ItemProcessor<Ban, Ban> processor(
	        @Value("#{jobParameters['codePostal']}") Integer codePostal,
	       @Value("#{jobParameters['codeInsee']}") Integer codeInsee
	) {
		
	    return ban -> {
	        if (codePostal == null && codeInsee == null) {
	            return ban;
	        }

	        boolean match = true;
	        if (codePostal != null) {
	            match &= codePostal.equals(ban.getCodePostal());
	        }
	        if (codeInsee != null) {
	            match &= codeInsee.equals(ban.getCodeInsee());
	        }

	        return match ? ban : null;
	    };
	}
	
	@Bean
	public ValidatingItemProcessor<Ban> validatingProcessor() {
	    ValidatingItemProcessor<Ban> validator = new ValidatingItemProcessor<>(new RowValidator());
	    validator.setFilter(true);
	    return validator;
	}
	
	@Bean
	public FlatFileItemReader<Ban> csvReader() {
		return new FlatFileItemReaderBuilder<Ban>()
			 .name("banCsvReader")
			 .resource(new ClassPathResource("adresses-79.csv"))
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
	                    "cad_parcelles")
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
			 .linesToSkip(1)
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
		);
	 """)
	 .beanMapped()
	 .build();
	}

}
