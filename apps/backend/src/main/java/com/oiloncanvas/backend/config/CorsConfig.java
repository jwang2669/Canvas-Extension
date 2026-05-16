package com.oiloncanvas.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Necessary configuration that allows cross-origin requests from the frontend (chrome extension runs on a different port).
 * Eventually, replace with the actual chrome extension URL instead of allowing all origins.
 */
@Configuration
public class CorsConfig {

    /**
     * Registers global CORS rules for backend endpoints.
     *
     * Current behavior allows all origins/headers/methods for development.
     * This should be narrowed to the extension origin in production.
     *
     * @return MVC configurer that applies CORS mapping rules
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}