package ar.edu.utn.dds.k3003.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

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
        log.info("✅ MetricsController inicializado para testing");
    }

    // Como el ejemplo: /number/{number} 
    @GetMapping("/gauge/{value}")
    public ResponseEntity<String> updateDebugGauge(@PathVariable Integer value) {
        debugGauge.set(value);
        log.info("🔧 Valor gauge cambiado a: {}", value);
        return ResponseEntity.ok("updated gauge: " + value);
    }

    @GetMapping("/test-counter")
    public ResponseEntity<String> testCounter() {
        meterRegistry.counter("dds.test", "source", "manual").increment();
        log.info("🧪 Counter de testing incrementado");
        return ResponseEntity.ok("counter incremented");
    }

    @GetMapping("/test-all-metrics")
    public ResponseEntity<String> testAllMetrics() {
        // Test counters
        meterRegistry.counter("dds.colecciones", "operation", "test", "status", "ok").increment();
        meterRegistry.counter("dds.hechos", "operation", "test", "status", "ok").increment();
        
        // Test gauge
        debugGauge.set(42);
        
        log.info("🧪 Todas las métricas de testing ejecutadas");
        return ResponseEntity.ok("all test metrics sent to datadog");
    }

    @GetMapping("/test-timer")
    public ResponseEntity<String> testTimer() {
        return meterRegistry.timer("dds.test.timer").record(() -> {
            try {
                // Simular trabajo
                Thread.sleep(100);
                log.info("⏱️ Timer de testing ejecutado");
                return ResponseEntity.ok("timer executed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.ok("timer interrupted");
            }
        });
    }

    @GetMapping("/test-error")
    public ResponseEntity<String> testError() {
        meterRegistry.counter("dds.test", "type", "error").increment();
        log.info("💥 Counter de error incrementado para testing");
        return ResponseEntity.ok("error counter incremented");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        meterRegistry.counter("dds.health", "status", "ok").increment();
        log.info("❤️ Health check ejecutado");
        return ResponseEntity.ok("metrics system healthy");
    }

    @GetMapping("/simulate-activity")
    public ResponseEntity<String> simulateActivity() {
        // Simular actividad de colecciones
        meterRegistry.counter("dds.colecciones", "operation", "listar", "status", "ok").increment();
        meterRegistry.counter("dds.colecciones", "operation", "crear", "status", "ok").increment();
        
        // Simular actividad de hechos
        meterRegistry.counter("dds.hechos", "operation", "crear", "status", "ok").increment();
        meterRegistry.counter("dds.hechos", "operation", "buscar", "status", "ok").increment();
        
        // Actualizar gauges
        debugGauge.set((int)(Math.random() * 100));
        
        log.info("🎭 Actividad simulada enviada a Datadog");
        return ResponseEntity.ok("activity simulated and sent to datadog");
    }
}