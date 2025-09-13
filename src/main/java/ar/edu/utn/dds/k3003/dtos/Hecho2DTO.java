package ar.edu.utn.dds.k3003.dtos;

import java.time.LocalDateTime;
import java.util.List;
import ar.edu.utn.dds.k3003.facades.dtos.CategoriaHechoEnum;

public record Hecho2DTO(String id, String nombreColeccion, String titulo, List<String> etiquetas,
                        CategoriaHechoEnum categoria,
                        String ubicacion, LocalDateTime fecha, String origen,
                        EstadoBorradoEnum estado, String descripcion) {

    public Hecho2DTO(String id, String nombreColeccion, String titulo) {
        this(id, nombreColeccion, titulo, null, null, null, null, null, null, null);
    }
}
