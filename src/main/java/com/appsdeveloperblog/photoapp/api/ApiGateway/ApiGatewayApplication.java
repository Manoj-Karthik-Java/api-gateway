package com.appsdeveloperblog.photoapp.api.ApiGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}

/*
Suppose you have two microservices registered with Eureka:

1. user-service
2. order-service

With this property enabled, you can call them via the gateway like:
spring.cloud.gateway.discovery.locator.enabled=true

http://localhost:8080/user-service/...
http://localhost:8080/order-service/...
Spring Cloud Gateway will:
1. Detect the services dynamically from Eureka.
2. Forward requests to the correct instances behind the scenes.

 */