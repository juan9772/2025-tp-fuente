// En ar.edu.utn.dds.k3003.procesadores
package ar.edu.utn.dds.k3003.procesadores;

import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.HechoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcesadorDeHechos implements Procesador {

    private final HechoRepository hechoRepository;
    private final ObjectMapper objectMapper; // Lo inyectaremos

    @Autowired
    public ProcesadorDeHechos(HechoRepository hechoRepository, ObjectMapper objectMapper) { // Inyectamos el bean
        this.hechoRepository = hechoRepository;
        this.objectMapper = objectMapper; // Lo asignamos
    }

    @java.lang.Override
    public java.lang.String tipo() {
        return "Hecho";
    }

    @java.lang.Override
    public void procesar(java.lang.String payload) {
        try {
            Hecho hecho = objectMapper.readValue(payload, Hecho.class);
            hechoRepository.save(hecho);
            System.out.println("Hecho guardado: " + hecho);
        } catch (IOException e) {
            System.err.println("Error al deserializar el payload a Hecho: " + e.getMessage());
        }
    }
}
