package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class ColeccionController {

    private static final Logger log = LoggerFactory.getLogger(ColeccionController.class);
    private final Fachada fachadaFuente;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger coleccionesActivasCount = new AtomicInteger(0);

    @Autowired
    public ColeccionController(Fachada fachadaFuente, MeterRegistry meterRegistry) {
        this.fachadaFuente = fachadaFuente;
        this.meterRegistry = meterRegistry;
        
        // Registrar gauge dinámico al inicializar
        meterRegistry.gauge("dds.colecciones.activas.count", coleccionesActivasCount);
    }

    @GetMapping("/colecciones")
    public ResponseEntity<List<ColeccionDTO>> listarColecciones() {
        log.debug("📋 Listando todas las colecciones");
        
        try {
            List<ColeccionDTO> colecciones = fachadaFuente.colecciones();
            
            // Gauge dinámico con el tamaño actual
            meterRegistry.gauge("dds.colecciones.total.count", colecciones.size());
            
            // Como el ejemplo: status=ok
            meterRegistry.counter("dds.colecciones", "operation", "listar", "status", "ok").increment();
            log.info("✅ Encontradas {} colecciones", colecciones.size());
            
            return ResponseEntity.ok(colecciones);
            
        } catch (Exception ex) {
            // Como el ejemplo: status=error
            log.error("❌ Error al listar colecciones", ex);
            meterRegistry.counter("dds.colecciones", "operation", "listar", "status", "error").increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/colecciones/{nombre}")
    public ResponseEntity<ColeccionDTO> obtenerColeccion(@PathVariable String nombre) {
        log.debug("🔍 Buscando colección: {}", nombre);
        
        try {
            ColeccionDTO coleccion = fachadaFuente.buscarColeccionXId(nombre);
            
            // Como el ejemplo: status=ok
            meterRegistry.counter("dds.colecciones", "operation", "buscar", "status", "ok").increment();
            log.info("✅ Colección encontrada: {}", nombre);
            
            return ResponseEntity.ok(coleccion);
            
        } catch (Exception ex) {
            // Como el ejemplo: status=error
            log.error("❌ Error al buscar colección {}", nombre, ex);
            meterRegistry.counter("dds.colecciones", "operation", "buscar", "status", "error").increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/colecciones/{nombre}/hechos")
    public ResponseEntity<List<HechoDTO>> obtenerHechosXColeccion(@PathVariable String nombre) {
        log.debug("🔍 Buscando hechos para colección: {}", nombre);
        
        try {
            List<HechoDTO> hechos = fachadaFuente.buscarHechosXColeccion(nombre);
            
            // Gauge dinámico con hechos por colección - usando Tags correctamente
            meterRegistry.gauge("dds.hechos.por.coleccion", 
                io.micrometer.core.instrument.Tags.of("coleccion", nombre), 
                hechos.size());
            
            // Como el ejemplo: status=ok
            meterRegistry.counter("dds.colecciones", "operation", "buscar_hechos", "status", "ok").increment();
            log.info("✅ Encontrados {} hechos para colección {}", hechos.size(), nombre);
            
            return ResponseEntity.ok(hechos);
            
        } catch (Exception ex) {
            // Como el ejemplo: status=error
            log.error("❌ Error al buscar hechos para colección {}", nombre, ex);
            meterRegistry.counter("dds.colecciones", "operation", "buscar_hechos", "status", "error").increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/colecciones")
    public ResponseEntity<ColeccionDTO> crearColeccion(@RequestBody ColeccionDTO coleccion) {
        log.debug("📝 Creando nueva colección: {}", coleccion.nombre());
        
        try {
            ColeccionDTO resultado = fachadaFuente.agregar(coleccion);
            
            // Actualizar gauge dinámico
            coleccionesActivasCount.incrementAndGet();
            
            // Como el ejemplo: status=ok
            meterRegistry.counter("dds.colecciones", "operation", "crear", "status", "ok").increment();
            log.info("✅ Colección creada exitosamente: {}", resultado.nombre());
            
            return ResponseEntity.ok(resultado);
            
        } catch (IllegalArgumentException ex) {
            // Como el ejemplo: status=rejected
            log.warn("⚠️ Colección no aprobada: {}", ex.getMessage());
            meterRegistry.counter("dds.colecciones", "operation", "crear", "status", "rejected").increment();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
            
        } catch (Exception ex) {
            // Como el ejemplo: status=error
            log.error("❌ Error al crear colección", ex);
            meterRegistry.counter("dds.colecciones", "operation", "crear", "status", "error").increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @DeleteMapping("colecciones")
    public ResponseEntity<Void> borrarTodo() {
        this.fachadaFuente.borrarAllColecciones();
        return ResponseEntity.noContent().build();
    }
} 