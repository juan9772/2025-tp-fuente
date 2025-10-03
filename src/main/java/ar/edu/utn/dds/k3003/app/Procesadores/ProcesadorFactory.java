// En ar.edu.utn.dds.k3003.procesadores
package ar.edu.utn.dds.k3003.procesadores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProcesadorFactory {
    private final Map<String, Procesador> procesadores;

    // Spring inyectará automáticamente una lista con TODAS las clases que implementen Procesador
    @Autowired
    public ProcesadorFactory(List<Procesador> procesadoresList) {
        // Convertimos la lista a un mapa donde la clave es el tipo() y el valor es el procesador
        this.procesadores = procesadoresList.stream()
                .collect(Collectors.toMap(Procesador::tipo, Function.identity()));
    }

    public Procesador get(String tipo) {
        Procesador procesador = procesadores.get(tipo);
        if (procesador == null) {
            throw new IllegalArgumentException("No se encontró un procesador para el tipo: " + tipo);
        }
        return procesador;
    }
}
