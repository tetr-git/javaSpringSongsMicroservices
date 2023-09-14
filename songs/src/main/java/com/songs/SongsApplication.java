package com.songs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SongsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SongsApplication.class, args);
	}



}


