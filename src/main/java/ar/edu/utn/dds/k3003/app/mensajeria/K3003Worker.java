package ar.edu.utn.dds.k3003.mensajeria;

import ar.edu.utn.dds.k3003.procesadores.ProcesadorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // <-- IMPORT NECESARIO

@Component
public class K3003Worker {

    private final ObjectMapper objectMapper;
    private final ProcesadorFactory procesadorFactory;

    @Autowired
    public K3003Worker(ProcesadorFactory procesadorFactory, ObjectMapper objectMapper) {
        this.procesadorFactory = procesadorFactory;
        this.objectMapper = objectMapper;
    }

    /**
     * Este es el método que usa NUESTRO ListenerService, que ya convierte
     * el mensaje a String gracias al SimpleMessageConverter.
     */
    public void procesarMensaje(java.lang.String payload) {
        System.out.println("Procesando mensaje recibido (desde String):");
        System.out.println(payload);
        this.procesar(payload);
    }

    /**
     * ¡LA SOLUCIÓN!
     * Este método es el "fallback" para el listener de la dependencia externa.
     * Atrapa el mensaje como un array de bytes y lo convierte a String antes de procesarlo.
     * Esto evita el NoSuchMethodException.
     */
    public void procesarMensaje(byte[] payload) {
        System.out.println("Procesando mensaje recibido (desde byte[]):");
        java.lang.String payloadComoString = new String(payload, StandardCharsets.UTF_8);
        System.out.println(payloadComoString);
        this.procesar(payloadComoString);
    }

    /**
     * Método privado para no duplicar la lógica de procesamiento.
     */
    private void procesar(java.lang.String payload) {
        try {
            // 1. Deserializamos el contenedor genérico 'Mensaje'
            Mensaje mensaje = objectMapper.readValue(payload, Mensaje.class);

            // 2. Obtenemos el procesador adecuado usando el Factory
            procesadorFactory.get(mensaje.getTipo())
                    .procesar(mensaje.getPayload()); // 3. Delegamos el procesamiento

            System.out.println("Mensaje de tipo '" + mensaje.getTipo() + "' procesado correctamente.");

        } catch (IOException e) {
            System.err.println("Error al deserializar el contenedor del mensaje: " + e.getMessage());
        } catch (java.lang.IllegalArgumentException e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
        }
    }
}
