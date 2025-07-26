package com.justin.server;

import com.justin.handlers.JsonCallMessageEncoder;
import com.justin.handlers.JsonMessageDecoder;
import com.justin.handlers.RpcServerMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;

public class RpcServer {

    @Value("${justin.rpc.server.port}")
    private int port;
    @Value("${justin.rpc.server.worker}")
    private int workerGroupSize;
    @Value("${justin.rpc.server.backlog}")
    private int backlogSize;

    public void startServer() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();
    }

    private void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(workerGroupSize);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, backlogSize)
                    .childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new RpcServerMessageHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("Server bind to port 11111");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
