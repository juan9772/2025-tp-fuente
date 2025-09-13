package ar.edu.utn.dds;

import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDMetricsUtils {
	private static final Logger log = LoggerFactory.getLogger(DDMetricsUtils.class);
	
	private final StepMeterRegistry registry;

	public DDMetricsUtils(String appTag) {
		log.info("Initializing Datadog metrics for app: {}", appTag);
		
		// crea un registro para nuestras métricas basadas en DD
		var config = new DatadogConfig() {
			@Override
			public Duration step() {
				return Duration.ofSeconds(10);
			}

			@Override
			public String apiKey() {
				String apiKey = System.getenv("DDAPI");
				if (apiKey == null || apiKey.trim().isEmpty()) {
					log.warn("⚠️  DDAPI environment variable not set! Datadog metrics will not work.");
					return "dummy-key"; // Fallback to prevent crashes
				}
				log.info("✅ Datadog API key configured correctly");
				return apiKey;
			}

			@Override
			public String uri() {
				return "https://api.us5.datadoghq.com";
			}

			@Override
			public String get(String k) {
				return null; // accept the rest of the defaults
			}
		};
		
		registry = new DatadogMeterRegistry(config, Clock.SYSTEM);
		registry.config().commonTags("app", appTag, "environment", "development", "service", "fuente");
		
		log.info("Datadog registry created with tags: app={}, environment=development, service=fuente", appTag);
		
		initInfraMonitoring();
	}

	public StepMeterRegistry getRegistry() {
		return registry;
	}

	private void initInfraMonitoring() {
		log.info("Initializing infrastructure monitoring metrics...");
		
		// agregamos a nuestro registro de métricas todo lo relacionado a infra/tech
		// de la instancia y JVM
		try (var jvmGcMetrics = new JvmGcMetrics(); 
			 var jvmHeapPressureMetrics = new JvmHeapPressureMetrics()) {
			jvmGcMetrics.bindTo(registry);
			jvmHeapPressureMetrics.bindTo(registry);
		}
		new JvmMemoryMetrics().bindTo(registry);
		new ProcessorMetrics().bindTo(registry);
		new FileDescriptorMetrics().bindTo(registry);
		
		log.info("✅ Infrastructure monitoring metrics initialized");
	}
}