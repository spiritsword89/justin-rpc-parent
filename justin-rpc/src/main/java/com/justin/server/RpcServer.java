package com.justin.server;

import com.justin.handlers.JsonCallMessageEncoder;
import com.justin.handlers.JsonMessageDecoder;
import com.justin.handlers.RpcServerMessageHandler;
import com.justin.handlers.ServerHeartbeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

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

        //readerIdleTime: triggers an event if nothing is read from the channel in this time
        //writerIdleTime: triggers an event if nothing is written to this channel in this time
        //allIdleTime: trigger event if neither read nor write occurs in this time

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

                            //if no read happens in 10 seconds, it triggers a user event.
                            //we need to provide a hearbeat handler to catch and process this event.
                            //if no read happens in 10 seconds, an event is triggered, we consider this connection is dead.
                            socketChannel.pipeline().addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                            socketChannel.pipeline().addLast(new ServerHeartbeatHandler());
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
