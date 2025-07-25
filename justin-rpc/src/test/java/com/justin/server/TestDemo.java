package com.justin.server;

import org.junit.jupiter.api.Test;

public class TestDemo {

    @Test
    public void run() throws InterruptedException {
        RpcServer rpcServer = new RpcServer();
        rpcServer.startServer();
    }
}
