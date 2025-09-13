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
}