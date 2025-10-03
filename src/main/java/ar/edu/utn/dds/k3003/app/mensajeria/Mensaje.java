package ar.edu.utn.dds.k3003.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.HechoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.Data;
@Data
public class Mensaje {
    private String tipo;      // Ej: "Hecho", "Alerta", "Configuracion", etc.
    private String payload;   // El JSON del objeto original como un String
}