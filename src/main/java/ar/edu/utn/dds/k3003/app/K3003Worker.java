package ar.edu.utn.dds.k3003.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.HechoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class K3003Worker {
    private final HechoRepository hechoRepository;

    @Autowired
    public K3003Worker(HechoRepository hechoRepository) {
        this.hechoRepository = hechoRepository;
    }

    // Escucha la cola configurada en spring.rabbitmq.queue.name
    @RabbitListener(queues = "mspsbdsj")
    public void receiveMessage(String payload) {
        System.out.println("Demora..");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("se recibio el siguiente payload:");
        System.out.println(payload);

        // Procesar el payload como JSON y cargar un hecho completo
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            Hecho hecho = mapper.readValue(payload, Hecho.class);
            hechoRepository.save(hecho);
            System.out.println("Hecho guardado: " + hecho);
        } catch (IOException e) {
            System.err.println("Error al deserializar el payload a Hecho: " + e.getMessage());
        }
    }
}
