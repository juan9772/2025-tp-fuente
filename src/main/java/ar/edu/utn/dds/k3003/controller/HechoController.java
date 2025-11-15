package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.dtos.EstadoBorradoEnum;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class HechoController {

    private static final Logger log = LoggerFactory.getLogger(HechoController.class);
    private final FachadaFuente fachadaFuente;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger hechosActivosCount = new AtomicInteger(0);
    
    // Counters creados una sola vez para mejor performance
    private final Counter hechosBusquedaOkCounter;
    private final Counter hechosBusquedaErrorCounter;
    private final Counter hechosCrearOkCounter;
    private final Counter hechosCrearRejectedCounter;
    private final Counter hechosCrearErrorCounter;

    @Autowired
    public HechoController(FachadaFuente fachadaFuente, MeterRegistry meterRegistry) {
        this.fachadaFuente = fachadaFuente;
        this.meterRegistry = meterRegistry;
        
        // Registrar gauge dinámico al inicializar
        meterRegistry.gauge("dds.hechos.activos.count", hechosActivosCount);
        
        // Crear todos los counters una sola vez
        this.hechosBusquedaOkCounter = Counter.builder("dds.hechos")
            .tag("operation", "buscar")
            .tag("status", "ok")
            .description("Búsquedas exitosas de hechos")
            .register(meterRegistry);
            
        this.hechosBusquedaErrorCounter = Counter.builder("dds.hechos")
            .tag("operation", "buscar")
            .tag("status", "error")
            .description("Búsquedas fallidas de hechos")
            .register(meterRegistry);
            
        this.hechosCrearOkCounter = Counter.builder("dds.hechos")
            .tag("operation", "crear")
            .tag("status", "ok")
            .description("Hechos creados exitosamente")
            .register(meterRegistry);
            
        this.hechosCrearRejectedCounter = Counter.builder("dds.hechos")
            .tag("operation", "crear")
            .tag("status", "rejected")
            .description("Hechos rechazados por validación")
            .register(meterRegistry);
            
        this.hechosCrearErrorCounter = Counter.builder("dds.hechos")
            .tag("operation", "crear")
            .tag("status", "error")
            .description("Errores al crear hechos")
            .register(meterRegistry);
            
        log.info("✅ HechoController inicializado con métricas optimizadas");
    }

    //    @GetMapping
    //    public ResponseEntity<List<HechoDTO>> listarHechos() {
    //        return ResponseEntity.ok(fachadaFuente.Hechos());
    //    }

    @GetMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> obtenerHecho(@PathVariable String id) {
        log.debug("🔍 Buscando hecho por ID: {}", id);
        
        try {
            HechoDTO hecho = fachadaFuente.buscarHechoXId(id);
            hechosBusquedaOkCounter.increment(); // Usar counter pre-creado
            log.info("✅ Hecho encontrado: {}", id);
            return ResponseEntity.ok(hecho);
            
        } catch (Exception ex) {
            log.error("❌ Error al buscar hecho {}", id, ex);
            hechosBusquedaErrorCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/hecho")
    public ResponseEntity<HechoDTO> crearHecho(@RequestBody HechoDTO hecho) {
        log.debug("📝 Creando nuevo hecho");
        
        try {
            HechoDTO resultado = fachadaFuente.agregar(hecho);
            hechosActivosCount.incrementAndGet(); // Actualizar gauge
            hechosCrearOkCounter.increment(); // Usar counter pre-creado
            log.info("✅ Hecho creado exitosamente con ID: {}", resultado.getId());
            return ResponseEntity.ok(resultado);
            
        } catch (IllegalArgumentException ex) {
            log.warn("⚠️ Hecho no aprobado: {}", ex.getMessage());
            hechosCrearRejectedCounter.increment();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
            
        } catch (Exception ex) {
            log.error("❌ Error al crear hecho", ex);
            hechosCrearErrorCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PatchMapping("/hecho/{id}")
    public ResponseEntity<HechoDTO> actualizarEstadoHecho(@PathVariable String id, @RequestBody Map<String, String> estadoData) {
        try {
            String estadoStr = estadoData.get("estado");
            EstadoBorradoEnum nuevoEstado = EstadoBorradoEnum.valueOf(estadoStr);
            HechoDTO resultado = fachadaFuente.modificar(id, nuevoEstado);
            
            // Si se marca como borrado, decrementar el gauge
            if (nuevoEstado == EstadoBorradoEnum.BORRADO) {
                hechosActivosCount.decrementAndGet();
                log.info("📉 Hecho {} marcado como borrado. Total activos: {}", id, hechosActivosCount.get());
            }
            
            return ResponseEntity.ok(resultado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
