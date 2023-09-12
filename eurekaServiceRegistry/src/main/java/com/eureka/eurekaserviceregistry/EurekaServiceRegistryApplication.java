package com.eureka.eurekaserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer // This annotation is used to enable the Eureka server
@SpringBootApplication
public class EurekaServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServiceRegistryApplication.class, args);
    }

}

