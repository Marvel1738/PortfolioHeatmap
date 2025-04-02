package com.PortfolioHeatmap;

/**
 * The main entry point for the Portfolio Heatmap Spring Boot application.
 * This class initializes the Spring Boot application, scans for components and entities,
 * and starts the application context.
 * 
 * @author [Marvel Bana]
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = "com.PortfolioHeatmap") // Scan everything in this root package
@EntityScan(basePackages = "com.PortfolioHeatmap.models") // Ensure models are scanned
@EnableCaching
public class PortfolioHeatmapApplication {

	// The main method that serves as the entry point for the application.
	// Starts the Spring Boot application context using SpringApplication.run.
	public static void main(String[] args) {
		SpringApplication.run(PortfolioHeatmapApplication.class, args);
	}

}