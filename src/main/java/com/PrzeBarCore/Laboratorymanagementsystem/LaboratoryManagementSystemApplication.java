package com.PrzeBarCore.Laboratorymanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LaboratoryManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaboratoryManagementSystemApplication.class, args);
	}

}
