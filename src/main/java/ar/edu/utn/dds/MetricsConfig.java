package ar.edu.utn.dds;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        // Usando DDMetricsUtils similar al ejemplo de Javalin
        DDMetricsUtils metricsUtils = new DDMetricsUtils("fuente");
        return metricsUtils.getRegistry();
    }
}