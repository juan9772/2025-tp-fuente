package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.client.ProcesadorPdIProxy;
import ar.edu.utn.dds.k3003.dtos.PdI_DTO;
import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.dtos.EstadoBorradoEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
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
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class HechoController {

    private static final Logger log = LoggerFactory.getLogger(HechoController.class);
    private final Fachada fachadaFuente;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger hechosActivosCount = new AtomicInteger(0);
    private final ProcesadorPdIProxy procesadorPdi;

    @Autowired
    public HechoController(Fachada fachadaFuente, MeterRegistry meterRegistry, ObjectMapper objectMapper ) {
        this.fachadaFuente = fachadaFuente;
        this.meterRegistry = meterRegistry;
        this.procesadorPdi=new ProcesadorPdIProxy(objectMapper);
        // Registrar gauge dinámico al inicializar
        meterRegistry.gauge("dds.hechos.activos.count", hechosActivosCount);
    }

    //    @GetMapping
    //    public ResponseEntity<List<HechoDTO>> listarHechos() {
    //        return ResponseEntity.ok(fachadaFuente.Hechos());
    //    }

    @GetMapping("/hechos/{id}")
    public ResponseEntity<HechoDTO> obtenerHecho(@PathVariable String id) {
        log.debug("🔍 Buscando hecho por ID: {}", id);
        
        try {
            HechoDTO hecho = fachadaFuente.buscarHechoXId(id);
            
            // Como el ejemplo: status=ok
            meterRegistry.counter("dds.hechos", "operation", "buscar", "status", "ok").increment();
            log.info("✅ Hecho encontrado: {}", id);
            
            return ResponseEntity.ok(hecho);
            
        } catch (Exception ex) {
            // Como el ejemplo: status=error
            log.error("❌ Error al buscar hecho {}", id, ex);
            meterRegistry.counter("dds.hechos", "operation", "buscar", "status", "error").increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

// src/main/java/ar/edu/utn/dds/k3003/controller/HechoController.java

    @GetMapping("/hechos")
    public ResponseEntity<List<HechoDTO>> obtenerHechos() {
        try {
            List<HechoDTO> hechos = fachadaFuente.obtenerHechos();
            return ResponseEntity.ok(hechos);
        } catch (Exception ex) {
            log.error("❌ Error al obtener hechos", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/hechos")
    public ResponseEntity<HechoDTO> crearHecho(@RequestBody HechoDTO hecho) {
        log.debug("📝 Creando nuevo hecho");
        
        try {
            HechoDTO resultado = fachadaFuente.agregar(hecho);
            
            // Actualizar gauge dinámico
            hechosActivosCount.incrementAndGet();
            
            // Como el ejemplo: status=ok  
            meterRegistry.counter("dds.hechos", "operation", "crear", "status", "ok").increment();
            log.info("✅ Hecho creado exitosamente con ID: {}", resultado.id());
            
            return ResponseEntity.ok(resultado);
            
        } catch (IllegalArgumentException ex) {
            // Como el ejemplo: status=rejected
            log.warn("⚠️ Hecho no aprobado: {}", ex.getMessage());
            meterRegistry.counter("dds.hechos", "operation", "crear", "status", "rejected").increment();
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
            
        } catch (Exception ex) {
            // Como el ejemplo: status=error
            log.error("❌ Error al crear hecho", ex);
            meterRegistry.counter("dds.hechos", "operation", "crear", "status", "error").increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PatchMapping("/hechos/{id}")
    public ResponseEntity<HechoDTO> actualizarEstadoHecho(@PathVariable String id, @RequestBody Map<String, String> estadoData) {
        try {
            String estado = estadoData.get("estado");
            return ResponseEntity.ok(fachadaFuente.modificar(id, EstadoBorradoEnum.valueOf(estado)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/hechos")
    public ResponseEntity<Void> borrarTodo() {
        this.fachadaFuente.borrarAllHechos();
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/pdis")
    public ResponseEntity<PdI_DTO> crearPdI(@RequestBody PdI_DTO pdIDTO) throws IOException {
        return ResponseEntity.ok(procesadorPdi.procesar(pdIDTO));
    }
}
