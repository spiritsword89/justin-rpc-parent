package com.justin_demo;

import com.justin.config.EnableRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpcClient(clientId = "demo-user", basePackages = "com.justin_demo.user.service")
public class DemoUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoUserApplication.class, args);
    }
}
