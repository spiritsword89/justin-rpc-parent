package com.justin_demo;

import com.justin.config.EnableRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpcServer
public class RpcServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcServerApplication.class, args);
    }
}
