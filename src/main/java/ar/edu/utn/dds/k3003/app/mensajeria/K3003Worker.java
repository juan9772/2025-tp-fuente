package ar.edu.utn.dds.k3003.mensajeria;

import ar.edu.utn.dds.k3003.mensajeria.Mensaje;
import ar.edu.utn.dds.k3003.procesadores.ProcesadorFactory; // Importamos el Factory que crearemos
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class K3003Worker {

    private final ObjectMapper objectMapper;
    private final ProcesadorFactory procesadorFactory; // Inyectamos el Factory

    @Autowired
    public K3003Worker(ProcesadorFactory procesadorFactory) {
        this.procesadorFactory = procesadorFactory;
        this.objectMapper = new ObjectMapper(); // El ObjectMapper es reutilizable y thread-safe
    }

    @RabbitListener(queues = "mspsbdsj")
    public void receiveMessage(String payload) {
        System.out.println("Mensaje recibido:");
        System.out.println(payload);

        try {
            // 1. Deserializamos el contenedor genérico 'Mensaje'
            Mensaje mensaje = objectMapper.readValue(payload, Mensaje.class);

            // 2. Obtenemos el procesador adecuado usando el Factory
            // El 'get' puede lanzar una excepción si el tipo no es soportado, lo cual es bueno.
            procesadorFactory.get(mensaje.getTipo())
                    .procesar(mensaje.getPayload()); // 3. Delegamos el procesamiento

            System.out.println("Mensaje de tipo '" + mensaje.getTipo() + "' procesado correctamente.");

        } catch (IOException e) {
            System.err.println("Error al deserializar el contenedor del mensaje: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
        }
    }
}
