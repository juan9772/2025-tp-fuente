package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.client.ProcesadorPdIProxy;
import ar.edu.utn.dds.k3003.dtos.EstadoBorradoEnum;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.model.Coleccion;
import ar.edu.utn.dds.k3003.model.Hecho;
import ar.edu.utn.dds.k3003.repository.*;

import lombok.val;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.amqp.core.Queue; // <-- IMPORT NECESARIO
import org.springframework.amqp.rabbit.core.RabbitAdmin; // <-- IMPORT NECESARIO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Fachada implements FachadaFuente {
    private final ColeccionRepository coleccionRepo;
    private final HechoRepository hechoRepo;
    private ProcesadorPdIProxy procesadorPdI;
    private final RabbitAdmin rabbitAdmin; // <-- AÑADIR VARIABLE MIEMBRO

    @Autowired
    public Fachada(ColeccionRepository coleccionRepository, HechoRepository hechoRepository, RabbitAdmin rabbitAdmin) { // <-- AÑADIR AL CONSTRUCTOR
        this.coleccionRepo = coleccionRepository;
        this.hechoRepo = hechoRepository;
        this.rabbitAdmin = rabbitAdmin; // <-- ASIGNAR EN CONSTRUCTOR
    }

    // Dejamos el constructor sin argumentos por si es usado en tests, pero lo ideal sería eliminarlo
    // o adaptarlo para inyectar mocks.
    public Fachada(){
        this.coleccionRepo = new InMemoryColeccionRepo();
        this.hechoRepo = new InMemoryHechoRepo();
        this.rabbitAdmin = null; // No estará disponible si se usa este constructor
    }

    //----------------------------------------------------------------------------------
    // NUEVO MÉTODO PARA CREAR UNA COLA DESDE EL CÓDIGO
    //----------------------------------------------------------------------------------
    /**
     * Crea una nueva cola en RabbitMQ de forma programática.
     * La cola será durable (sobrevivirá reinicios del broker).
     *
     * @param nombreCola El nombre de la cola que se desea crear.
     * @return El nombre de la cola creada o un mensaje indicando que ya existía.
     */
    public java.lang.String crearCola(java.lang.String nombreCola) {
        if (this.rabbitAdmin == null) {
            throw new IllegalStateException("RabbitAdmin no está disponible. Asegúrese de que la aplicación se inicie con el contexto de Spring.");
        }
        // El método declareQueue es "idempotente": si la cola ya existe con las mismas
        // propiedades, no hace nada. Si no existe, la crea.
        java.lang.String resultado = this.rabbitAdmin.declareQueue(new Queue(nombreCola, true));

        if (resultado != null) {
            System.out.println("Cola '" + nombreCola + "' creada exitosamente.");
            return nombreCola;
        } else {
            System.out.println("La cola '" + nombreCola + "' ya existía, no se realizaron cambios.");
            return "La cola '" + nombreCola + "' ya existía.";
        }
    }
    //----------------------------------------------------------------------------------


    @java.lang.Override
    public ColeccionDTO agregar(ColeccionDTO coleccionDTO) {
        if (this.coleccionRepo.findById(coleccionDTO.nombre()).isPresent()) {
            throw new IllegalArgumentException(coleccionDTO.nombre() + " ya existe");
        }
        val coleccion = new Coleccion(coleccionDTO.nombre(), coleccionDTO.descripcion());
        this.coleccionRepo.save(coleccion);
        return new ColeccionDTO(coleccion.getNombre(), coleccion.getDescripcion());
    }

    // ... EL RESTO DE MÉTODOS DE LA FACHADA PERMANECEN IGUAL ...

    @java.lang.Override
    public ColeccionDTO buscarColeccionXId(java.lang.String coleccionId) throws NoSuchElementException {
        val coleccionOptional = this.coleccionRepo.findById(coleccionId);
        if (coleccionOptional.isEmpty()) {
            throw new NoSuchElementException(coleccionId + " no existe");
        }
        val coleccion = coleccionOptional.get();
        return new ColeccionDTO(coleccion.getNombre(), coleccion.getDescripcion());
    }

    @java.lang.Override
    public HechoDTO agregar(HechoDTO hechoDTO) {
        if (Objects.equals(hechoDTO.nombreColeccion().trim(), "")) {
            throw new IllegalArgumentException(hechoDTO.id() + " no se paso nombre de coleccion");
        }
        /*
        //al añadir un hecho valido que exista una coleccion con el nombre de coleccion que tiene el hecho
        if (coleccionRepo.findById(hechoDTO.nombreColeccion()).isEmpty()) {
            throw new IllegalArgumentException(hechoDTO.nombreColeccion() + " no existe coleccion con ese nombre");
        }
        */
        if (this.coleccionRepo.findById(hechoDTO.id()).isPresent()) {
            throw new IllegalArgumentException(hechoDTO.id() + " ya existe");
        }
        Hecho hecho =
                new Hecho(
                        hechoDTO.nombreColeccion(),
                        hechoDTO.titulo(),
                        hechoDTO.etiquetas(),
                        hechoDTO.categoria(),
                        hechoDTO.ubicacion(),
                        hechoDTO.fecha(),
                        hechoDTO.origen());
        // guardo en el repo y devuelvo el dto
        val resultadoHecho = hechoRepo.save(hecho);
        return new HechoDTO(
                resultadoHecho.getId().toString(),
                resultadoHecho.getNombreColeccion(),
                resultadoHecho.getTitulo(),
                resultadoHecho.getEtiquetas(),
                resultadoHecho.getCategoria(),
                resultadoHecho.getUbicacion(),
                resultadoHecho.getFecha(),
                resultadoHecho.getOrigen());
    }

    @java.lang.Override
    public HechoDTO buscarHechoXId(java.lang.String hechoId) throws NoSuchElementException {
        val hechoOptional = this.hechoRepo.findById(hechoId);
        if (hechoOptional.isEmpty()) {
            throw new NoSuchElementException(hechoId + " no existe");
        }
        val resultadoHecho = hechoOptional.get();
        return new HechoDTO(
                resultadoHecho.getId().toString(),
                resultadoHecho.getNombreColeccion(),
                resultadoHecho.getTitulo(),
                resultadoHecho.getEtiquetas(),
                resultadoHecho.getCategoria(),
                resultadoHecho.getUbicacion(),
                resultadoHecho.getFecha(),
                resultadoHecho.getOrigen());
    }

    @java.lang.Override
    public java.util.List<HechoDTO> buscarHechosXColeccion(java.lang.String s) throws NoSuchElementException {
        val coleccionOptional = this.coleccionRepo.findById(s);
        if (coleccionOptional.isEmpty()) {
            throw new NoSuchElementException(s + " no existe coleccion con ese nombre");
        }
        val hechos = this.hechoRepo.findAll();
        return hechos.stream()
                .filter(hecho -> s.equals(hecho.getNombreColeccion()))
                .filter(hecho -> hecho.getEstado().equals(EstadoBorradoEnum.NO_BORRADO)) // <-- Condición adicional para filtrar por estado
                .map(hecho -> new HechoDTO(
                        hecho.getId().toString(),
                        hecho.getNombreColeccion(),
                        hecho.getTitulo(),
                        hecho.getEtiquetas(),
                        hecho.getCategoria(),
                        hecho.getUbicacion(),
                        hecho.getFecha(),
                        hecho.getOrigen()))
                .toList();
    }

    @java.lang.Override
    public void setProcesadorPdI(ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI fachadaProcesadorPdI) {
        // Dummy implementation, not relevant for this part
    }

    @java.lang.Override
    public ar.edu.utn.dds.k3003.facades.dtos.PdIDTO agregar(ar.edu.utn.dds.k3003.facades.dtos.PdIDTO pdIDTO) {
        // Dummy implementation
        return null;
    }

    @java.lang.Override
    public java.util.List<ColeccionDTO> colecciones(){
        return this.coleccionRepo.findAll().stream().map(coleccion -> new ColeccionDTO(coleccion.getNombre(), coleccion.getDescripcion()) ).toList();
    }

    public HechoDTO modificar(java.lang.String hechoId, EstadoBorradoEnum estado) throws NoSuchElementException {
        // 1. Busca el hecho por su ID en el repositorio
        java.util.Optional<Hecho> hechoOptional = hechoRepo.findById(hechoId);
        if (hechoOptional.isEmpty()) {
            throw new NoSuchElementException("Hecho no encontrado: " + hechoId);
        }

        // 2. Obtiene el objeto Hecho para modificarlo
        Hecho hecho = hechoOptional.get();

        // 3. Actualiza el estado
        hecho.setEstado(estado);

        // 4. Guarda los cambios en el repositorio
        Hecho hechoModificado = hechoRepo.save(hecho);

        // 5. Devuelve el DTO del hecho modificado
        return new HechoDTO(
                hechoModificado.getId().toString(),
                hechoModificado.getNombreColeccion(),
                hechoModificado.getTitulo(),
                null, null, null, null, null // Completar si es necesario
        );
    }

    public java.util.List<HechoDTO> obtenerHechos() {
        return hechoRepo.findAll().stream()
                .map(hecho -> new HechoDTO(
                        hecho.getId().toString(),
                        hecho.getNombreColeccion(),
                        hecho.getTitulo(),
                        hecho.getEtiquetas(),
                        hecho.getCategoria(),
                        hecho.getUbicacion(),
                        hecho.getFecha(),
                        hecho.getOrigen()))
                .toList();
    }

    public void borrarAllHechos() {
        hechoRepo.deleteAll();
    }
    public void borrarAllColecciones() {
        coleccionRepo.deleteAll();
    }
}
