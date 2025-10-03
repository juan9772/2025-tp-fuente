package ar.edu.utn.dds.k3003.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // <-- IMPORTANTE

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // <-- Le decimos a Spring que este es el ObjectMapper principal
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Permite que Spring sepa cómo manejar fechas como LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Ignora propiedades desconocidas en el JSON, muy útil para flexibilidad
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Formatea las fechas como strings en formato ISO (ej: "2025-10-03T15:00:00")
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }
}
