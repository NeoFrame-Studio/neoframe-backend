package com.neoframe.neoframe_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NeoframeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoframeBackendApplication.class, args);
	}

}
