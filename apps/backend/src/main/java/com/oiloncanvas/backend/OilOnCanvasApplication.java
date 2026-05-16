package com.oiloncanvas.backend;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Oil on Canvas backend API.
 * Serves the browser extension and future Canvas AI assistant features.
 */
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class OilOnCanvasApplication {

    public static void main(String[] args) {
        SpringApplication.run(OilOnCanvasApplication.class, args);
    }
}
