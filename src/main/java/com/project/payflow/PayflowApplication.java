package com.project.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableScheduling
@ConfigurationPropertiesScan
public class PayflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayflowApplication.class, args);
	}

}
