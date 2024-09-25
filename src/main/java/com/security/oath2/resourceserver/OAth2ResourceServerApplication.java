package com.security.oath2.resourceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OAth2ResourceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAth2ResourceServerApplication.class, args);
		System.out.println("------------------------------------------------Started------------------------------------------------");
	}

}
