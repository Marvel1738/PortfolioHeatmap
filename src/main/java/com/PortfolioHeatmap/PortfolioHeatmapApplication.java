package com.PortfolioHeatmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.PortfolioHeatmap") // Scan everything in this root package
@EntityScan(basePackages = "com.PortfolioHeatmap.models") // Ensure models are scanned
public class PortfolioHeatmapApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioHeatmapApplication.class, args);
	}

}
