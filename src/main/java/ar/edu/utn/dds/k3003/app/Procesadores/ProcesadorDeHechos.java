// En ar.edu.utn.dds.k3003.procesadores
package ar.edu.utn.dds.k3003.procesadores;

import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.HechoRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // Le decimos a Spring que gestione este componente
public class ProcesadorDeHechos implements Procesador {

    private final HechoRepository hechoRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProcesadorDeHechos(HechoRepository hechoRepository) {
        this.hechoRepository = hechoRepository;
        // Configuramos el ObjectMapper una sola vez en el constructor
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String tipo() {
        return "Hecho"; // Este procesador se encarga de los mensajes de tipo "Hecho"
    }

    @Override
    public void procesar(String payload) {
        try {
            Hecho hecho = objectMapper.readValue(payload, Hecho.class);
            hechoRepository.save(hecho);
            System.out.println("Hecho guardado: " + hecho);
        } catch (IOException e) {
            System.err.println("Error al deserializar el payload a Hecho: " + e.getMessage());
            // Opcional: relanzar una excepción para que el Worker la maneje
            // throw new RuntimeException("Fallo al procesar Hecho", e);
        }
    }
}
