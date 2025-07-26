package com.justin.server;

import com.justin.handlers.JsonCallMessageEncoder;
import com.justin.handlers.JsonMessageDecoder;
import com.justin.handlers.RpcServerMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RpcServer {

    public void startServer() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();

        System.out.println("Server started");
        Thread.sleep(20000);
    }

    private void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new RpcServerMessageHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(11111).sync();
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
