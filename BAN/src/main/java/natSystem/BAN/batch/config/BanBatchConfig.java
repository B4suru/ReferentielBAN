package natSystem.BAN.batch.config;

import java.util.Arrays;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import natSystem.BAN.batch.context.BanDiffContext;
import natSystem.BAN.tools.ParseTool;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import natSystem.BAN.batch.listener.BanStepListener;
import natSystem.BAN.batch.validator.RowValidator;
import natSystem.BAN.entity.Ban;


@Slf4j
@Configuration
public class BanBatchConfig {
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
				.<Ban, Ban>chunk(10000)
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
	        ItemProcessor<Ban, Ban> processorFilter,
	        ItemProcessor<Ban, Ban> processorDiff
	) {
	    CompositeItemProcessor<Ban, Ban> composite = new CompositeItemProcessor<>();
	    composite.setDelegates(Arrays.asList(validatingProcessor, processorFilter, processorDiff));
	    return composite;
	}
	
	@Bean
	@StepScope
	public ItemProcessor<Ban, Ban> processorFilter(
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
	@StepScope
	public ItemProcessor<Ban, Ban> processorDiff(BanDiffContext banDiffContext) {
		return ban -> {
			if (banDiffContext.getBdIds().remove(ban.getId())) {
				return null;
			} else {
				return ban;
			}
		};
	}

	@Bean
	@StepScope
	public ValidatingItemProcessor<Ban> validatingProcessor() {
	    ValidatingItemProcessor<Ban> validator = new ValidatingItemProcessor<>(new RowValidator());
	    validator.setFilter(true);
	    return validator;
	}
	
	@Bean
	@StepScope
	public FlatFileItemReader<Ban> csvReader() {
		return new FlatFileItemReaderBuilder<Ban>()
			 .name("banCsvReader")
			 .resource(new FileSystemResource("csv_sorted.csv"))
			 .delimited()
			 .delimiter(";")
			 .strict(false)
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
	                    "id_ban_commune"
)
			 .fieldSetMapper(fs -> {
				    Ban b = new Ban();
					ParseTool parseTool = new ParseTool();


				    b.setId(fs.readString("id"));
				    b.setNumero(parseTool.parseIntSafe(fs.readString("numero")));
				    b.setRep(fs.readString("rep"));
				    b.setNomVoie(fs.readString("nom_voie"));
				 	b.setCodePostal(parseTool.parseIntSafe(fs.readString("code_postal")));
				 	b.setCodeInsee(parseTool.parseIntSafe(fs.readString("code_insee")));
				    b.setNomCommune(fs.readString("nom_commune"));
					b.setX(parseTool.parseDoubleSafe(fs.readString("x")));
					b.setY(parseTool.parseDoubleSafe(fs.readString("y")));
					b.setLon(parseTool.parseDoubleSafe(fs.readString("lon")));
					b.setLat(parseTool.parseDoubleSafe(fs.readString("lat")));

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
