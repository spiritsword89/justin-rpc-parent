package com.justin.config;

import com.justin.server.RpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcServerConfiguration {

    @Bean
    public RpcServer rpcServer() {
        RpcServer rpcServer = new RpcServer();
        try {
            rpcServer.startServer();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return rpcServer;
    }
}
