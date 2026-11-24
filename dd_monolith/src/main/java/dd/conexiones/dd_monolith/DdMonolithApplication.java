package dd.conexiones.dd_monolith;

import dd.conexiones.dd_ms_auth.infrastructure.security.JwtAuthenticationFilter;
import dd.conexiones.dd_ms_auth.infrastructure.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * Empaquetado monolitico modular para entornos con RAM limitada (ej. Render Free 512 MB).
 * Ejecuta los 7 dominios (auth, crm, billing, network, support, comms, reports) y la
 * frontera de seguridad/CORS del gateway dentro de una unica JVM Spring Boot.
 */
@SpringBootApplication
@AutoConfigurationPackage(basePackages = {
        "dd.conexiones.dd_monolith",
        "dd.conexiones.dd_ms_auth",
        "dd.conexiones.dd_ms_crm",
        "dd.conexiones.dd_ms_billing",
        "dd.conexiones.dd_ms_network",
        "dd.conexiones.dd_ms_support",
        "dd.conexiones.dd_ms_comms",
        "dd.conexiones.dd_ms_reports"
})
@ComponentScan(
        basePackages = {
                "dd.conexiones.dd_monolith",
                "dd.conexiones.dd_ms_auth",
                "dd.conexiones.dd_ms_crm",
                "dd.conexiones.dd_ms_billing",
                "dd.conexiones.dd_ms_network",
                "dd.conexiones.dd_ms_support",
                "dd.conexiones.dd_ms_comms",
                "dd.conexiones.dd_ms_reports"
        },
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        SecurityConfig.class,
                        JwtAuthenticationFilter.class,
                        dd.conexiones.dd_ms_billing.infrastructure.config.RestClientConfig.class,
                        dd.conexiones.dd_ms_comms.infrastructure.config.RestClientConfig.class,
                        dd.conexiones.dd_ms_reports.infrastructure.config.RestClientConfig.class
                })
        }
)
public class DdMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdMonolithApplication.class, args);
    }
}
