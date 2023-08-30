package com.example.umc_insider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients("com.example.umc_insider.client")
public class UmcInsiderApplication {

	public static void main(String[] args) {
		SpringApplication.run(UmcInsiderApplication.class, args);
	}

}
