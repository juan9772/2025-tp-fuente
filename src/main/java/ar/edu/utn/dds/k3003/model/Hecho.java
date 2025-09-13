package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.CategoriaHechoEnum;
import ar.edu.utn.dds.k3003.dtos.EstadoBorradoEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Hecho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombreColeccion;
    private String titulo;
    private List<String> etiquetas;
    private CategoriaHechoEnum categoria;
    private String ubicacion;
    private LocalDateTime fecha;
    private String origen;

    // Nuevos campos para el estado de borrado
    @Enumerated(EnumType.STRING)
    private EstadoBorradoEnum estado;
    private String descripcion;

    // Constructor para la creación de un nuevo Hecho (usado en el método 'agregar')
    public Hecho(
            String nombreColeccion,
            String titulo,
            List<String> etiquetas,
            CategoriaHechoEnum categoria,
            String ubicacion,
            LocalDateTime fecha,
            String origen) {
        this.nombreColeccion = nombreColeccion;
        this.titulo = titulo;
        this.etiquetas = etiquetas;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.origen = origen;
        this.estado = EstadoBorradoEnum.NO_BORRADO; // Estado inicial por defecto
    }

    // Constructor completo (útil para pruebas o mapeo desde el DTO completo)
    public Hecho(Integer id, String nombreColeccion, String titulo, List<String> etiquetas, CategoriaHechoEnum categoria, String ubicacion, LocalDateTime fecha, String origen, EstadoBorradoEnum estado, String descripcion) {
        this.id = id;
        this.nombreColeccion = nombreColeccion;
        this.titulo = titulo;
        this.etiquetas = etiquetas;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.origen = origen;
        this.estado = estado;
        this.descripcion = descripcion;
    }
}
