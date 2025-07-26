package com.justin_demo;

import com.justin.config.EnableRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpcClient(clientId = "demo-booking", basePackages = "com.justin_demo.booking.service")
public class DemoBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoBookingApplication.class, args);
    }
}
