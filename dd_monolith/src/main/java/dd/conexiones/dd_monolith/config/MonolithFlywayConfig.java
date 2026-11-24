package dd.conexiones.dd_monolith.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Ejecuta Flyway de forma independiente sobre los 7 esquemas de PostgreSQL
 * (auth, crm, billing, network, support, comms, reports) conservando exactamente
 * las mismas tablas flyway_schema_history por esquema que el despliegue en microservicios.
 */
@Configuration
@ConditionalOnProperty(name = "monolith.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class MonolithFlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(MonolithFlywayConfig.class);

    public static final List<String> SCHEMAS = List.of(
            "dd_auth", "crm", "billing", "network", "support", "comms", "reports"
    );

    @Bean("monolithFlywayMigrator")
    public Object monolithFlywayMigrator(
            DataSource dataSource,
            @Value("${monolith.flyway.connect-retries:12}") int connectRetries,
            @Value("${monolith.flyway.connect-retries-interval:5}") int connectRetriesIntervalSeconds) {
        for (String schema : SCHEMAS) {
            log.info("Ejecutando migraciones Flyway para esquema '{}'", schema);
            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .createSchemas(true)
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .locations("classpath:db/monolith/" + schema)
                    .connectRetries(connectRetries)
                    .connectRetriesInterval(connectRetriesIntervalSeconds)
                    .load()
                    .migrate();
        }
        return new Object();
    }

    /** Garantiza que todas las migraciones corran antes de Hibernate ddl-auto: validate. */
    @Bean
    public static BeanFactoryPostProcessor flywayBeforeJpaPostProcessor() {
        return beanFactory -> {
            if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
                return;
            }
            BeanDefinition emfDef = beanFactory.getBeanDefinition("entityManagerFactory");
            Set<String> deps = new LinkedHashSet<>();
            if (emfDef.getDependsOn() != null) {
                deps.addAll(Arrays.asList(emfDef.getDependsOn()));
            }
            deps.add("monolithFlywayMigrator");
            emfDef.setDependsOn(deps.toArray(String[]::new));
        };
    }
}
