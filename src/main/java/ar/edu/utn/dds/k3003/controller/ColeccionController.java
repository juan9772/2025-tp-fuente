package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ColeccionController {

    private static final Logger log = LoggerFactory.getLogger(ColeccionController.class);
    private final Fachada fachadaFuente;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger coleccionesActivasCount = new AtomicInteger(0);
    private final AtomicInteger coleccionesTotalCount = new AtomicInteger(0);
    
    // Map para gauges dinámicos con tags (hechos por colección)
    private final Map<String, AtomicInteger> hechosPorColeccionGauges = new ConcurrentHashMap<>();
    
    // Counters creados una sola vez para mejor performance
    private final Counter coleccionesListarOkCounter;
    private final Counter coleccionesListarErrorCounter;
    private final Counter coleccionesBuscarOkCounter;
    private final Counter coleccionesBuscarErrorCounter;
    private final Counter coleccionesBuscarHechosOkCounter;
    private final Counter coleccionesBuscarHechosErrorCounter;
    private final Counter coleccionesCrearOkCounter;
    private final Counter coleccionesCrearRejectedCounter;
    private final Counter coleccionesCrearErrorCounter;

    @Autowired
    public ColeccionController(Fachada fachadaFuente, MeterRegistry meterRegistry) {
        this.fachadaFuente = fachadaFuente;
        this.meterRegistry = meterRegistry;
        
        // Registrar gauges al inicializar
        meterRegistry.gauge("dds.colecciones.activas.count", coleccionesActivasCount);
        meterRegistry.gauge("dds.colecciones.total.count", coleccionesTotalCount);
        
        // Crear todos los counters una sola vez
        this.coleccionesListarOkCounter = Counter.builder("dds.colecciones")
            .tag("operation", "listar").tag("status", "ok").register(meterRegistry);
        this.coleccionesListarErrorCounter = Counter.builder("dds.colecciones")
            .tag("operation", "listar").tag("status", "error").register(meterRegistry);
        this.coleccionesBuscarOkCounter = Counter.builder("dds.colecciones")
            .tag("operation", "buscar").tag("status", "ok").register(meterRegistry);
        this.coleccionesBuscarErrorCounter = Counter.builder("dds.colecciones")
            .tag("operation", "buscar").tag("status", "error").register(meterRegistry);
        this.coleccionesBuscarHechosOkCounter = Counter.builder("dds.colecciones")
            .tag("operation", "buscar_hechos").tag("status", "ok").register(meterRegistry);
        this.coleccionesBuscarHechosErrorCounter = Counter.builder("dds.colecciones")
            .tag("operation", "buscar_hechos").tag("status", "error").register(meterRegistry);
        this.coleccionesCrearOkCounter = Counter.builder("dds.colecciones")
            .tag("operation", "crear").tag("status", "ok").register(meterRegistry);
        this.coleccionesCrearRejectedCounter = Counter.builder("dds.colecciones")
            .tag("operation", "crear").tag("status", "rejected").register(meterRegistry);
        this.coleccionesCrearErrorCounter = Counter.builder("dds.colecciones")
            .tag("operation", "crear").tag("status", "error").register(meterRegistry);
            
        log.info("✅ ColeccionController inicializado con métricas optimizadas");
    }

    @GetMapping("/colecciones")
    public ResponseEntity<List<ColeccionDTO>> listarColecciones() {
        log.debug("📋 Listando todas las colecciones");
        
        try {
            List<ColeccionDTO> colecciones = fachadaFuente.colecciones();
            coleccionesTotalCount.set(colecciones.size()); // Actualizar gauge
            coleccionesListarOkCounter.increment(); // Usar counter pre-creado
            log.info("✅ Encontradas {} colecciones", colecciones.size());
            
            return ResponseEntity.ok(colecciones);
            
        } catch (Exception ex) {
            log.error("❌ Error al listar colecciones", ex);
            coleccionesListarErrorCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/colecciones/{nombre}")
    public ResponseEntity<ColeccionDTO> buscarColeccion(@PathVariable String nombre) {
        log.debug("🔍 Buscando colección: {}", nombre);
        
        try {
            ColeccionDTO coleccion = fachadaFuente.buscarColeccionXNombre(nombre);
            coleccionesBuscarOkCounter.increment();
            log.info("✅ Colección encontrada: {}", nombre);
            return ResponseEntity.ok(coleccion);
            
        } catch (Exception ex) {
            log.error("❌ Error al buscar colección {}", nombre, ex);
            coleccionesBuscarErrorCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/colecciones/{nombre}/hechos")
    public ResponseEntity<List<HechoDTO>> obtenerHechosXColeccion(@PathVariable String nombre) {
        log.debug("🔍 Buscando hechos para colección: {}", nombre);
        
        try {
            List<HechoDTO> hechos = fachadaFuente.buscarHechosXColeccion(nombre);
            
            // Actualizar gauge dinámico con tags correctos
            AtomicInteger gauge = hechosPorColeccionGauges.computeIfAbsent(nombre, key -> {
                AtomicInteger newGauge = new AtomicInteger(0);
                meterRegistry.gauge("dds.hechos.por.coleccion", 
                    Tags.of("coleccion", nombre), 
                    newGauge);
                return newGauge;
            });
            gauge.set(hechos.size());
            
            coleccionesBuscarHechosOkCounter.increment();
            log.info("✅ Encontrados {} hechos para colección {}", hechos.size(), nombre);
            return ResponseEntity.ok(hechos);
            
        } catch (Exception ex) {
            log.error("❌ Error al buscar hechos para colección {}", nombre, ex);
            coleccionesBuscarHechosErrorCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/colecciones")
    public ResponseEntity<ColeccionDTO> crearColeccion(@RequestBody ColeccionDTO coleccion) {
        log.debug("📝 Creando nueva colección: {}", coleccion.nombre());
        
        try {
            ColeccionDTO resultado = fachadaFuente.agregar(coleccion);
            coleccionesActivasCount.incrementAndGet(); // Actualizar gauge
            coleccionesCrearOkCounter.increment(); // Usar counter pre-creado
            log.info("✅ Colección creada exitosamente: {}", resultado.nombre());
            return ResponseEntity.ok(resultado);
            
        } catch (IllegalArgumentException ex) {
            log.warn("⚠️ Colección no aprobada: {}", ex.getMessage());
            coleccionesCrearRejectedCounter.increment();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
            
        } catch (Exception ex) {
            log.error("❌ Error al crear colección", ex);
            coleccionesCrearErrorCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @DeleteMapping("/colecciones")
    public ResponseEntity<Void> borrarTodo() {
        this.fachadaFuente.borrarAllColecciones();
        return ResponseEntity.noContent().build();
    }
} 