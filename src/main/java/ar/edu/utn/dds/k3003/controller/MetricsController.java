package ar.edu.utn.dds.k3003.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);
    private final AtomicInteger debugGauge = new AtomicInteger(0);
    private final MeterRegistry meterRegistry;

    @Autowired
    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        meterRegistry.gauge("dds.debug.gauge", debugGauge);
        log.info("✅ MetricsController inicializado para métricas útiles");
    }

    // Endpoint para consultar el total de colecciones activas
    @GetMapping("/colecciones/total")
    public ResponseEntity<Map<String, Object>> getTotalColecciones() {
        Gauge gauge = meterRegistry.find("dds.colecciones.total.count").gauge();
        double total = gauge != null ? gauge.value() : 0;
        return ResponseEntity.ok(Map.of("totalColecciones", (int) total));
    }

    // Endpoint para consultar el total de hechos activos
    @GetMapping("/hechos/total")
    public ResponseEntity<Map<String, Object>> getTotalHechos() {
        Gauge gauge = meterRegistry.find("dds.hechos.activos.count").gauge();
        double total = gauge != null ? gauge.value() : 0;
        return ResponseEntity.ok(Map.of("totalHechos", (int) total));
    }

    // Endpoint para consultar hechos por colección
    @GetMapping("/hechos/por-coleccion")
    public ResponseEntity<Map<String, Object>> getHechosPorColeccion() {
        // Corrige la línea en el método getHechosPorColeccion:
        Collection<Gauge> gauges = meterRegistry.get("dds.hechos.por.coleccion").gauges();
        Map<String, Integer> hechosPorColeccion = new HashMap<>();
        for (Gauge g : gauges) {
            String coleccion = g.getId().getTag("coleccion");
            hechosPorColeccion.put(coleccion, (int) g.value());
        }
        return ResponseEntity.ok(Map.of("hechosPorColeccion", hechosPorColeccion));
    }

    // Endpoint para consultar actividad reciente (contadores de operaciones)
    @GetMapping("/actividad")
    public ResponseEntity<Map<String, Object>> getActividad() {
        Map<String, Object> actividad = new HashMap<>();
        actividad.put("colecciones_creadas", getCounterValue("dds.colecciones", "operation", "crear"));
        actividad.put("colecciones_listadas", getCounterValue("dds.colecciones", "operation", "listar"));
        actividad.put("hechos_creados", getCounterValue("dds.hechos", "operation", "crear"));
        actividad.put("hechos_buscados", getCounterValue("dds.hechos", "operation", "buscar"));
        return ResponseEntity.ok(actividad);
    }

    private double getCounterValue(String name, String... tags) {
        Counter counter = meterRegistry.find(name).tags(tags).counter();
        return counter != null ? counter.count() : 0;
    }

    // Endpoint para cambiar el valor del gauge de debug
    @GetMapping("/gauge/{value}")
    public ResponseEntity<String> updateDebugGauge(@PathVariable Integer value) {
        debugGauge.set(value);
        log.info("🔧 Valor gauge cambiado a: {}", value);
        return ResponseEntity.ok("updated gauge: " + value);
    }
}