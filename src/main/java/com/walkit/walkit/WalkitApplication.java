package com.walkit.walkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WalkitApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalkitApplication.class, args);
	}

}
