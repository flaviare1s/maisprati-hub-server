package com.maisprati.hub.infrastructure.monitoring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/actuator")
public class MonitoringController {

    @GetMapping("/memory")
    public Map<String, Object> memory() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        return Map.of(
                "maxMemory", (maxMemory / (1024 * 1024)) + "MB",
                "totalMemory", (totalMemory / (1024 * 1024)) + "MB",
                "usedMemory", (usedMemory / (1024 * 1024)) + "MB",
                "freeMemory", (freeMemory / (1024 * 1024)) + "MB",
                "percentUsed", (usedMemory * 100 / maxMemory) + "%"
        );
    }
}
