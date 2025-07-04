package com.example.demo;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class ClassroomApplication {

	@PostConstruct
	public void init() {
		// Set default timezone to IST
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

	static {
		// Set system property for ImageIO plugins
		System.setProperty("javax.imageio.spi.ServiceRegistry.class", 
						   "sun.plugin.util.ServiceRegistry");
	}

	public static void main(String[] args) {
		SpringApplication.run(ClassroomApplication.class, args);
	}

}
