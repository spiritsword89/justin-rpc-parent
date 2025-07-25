package com.justin.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

// Core component
public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup worker = new NioEventLoopGroup();

    @Value("${justin.rpc.server.port}")
    private String port;

    @Value("${justin.rpc.client.host}")
    private String host;

    private Channel channel;

    public RpcClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();

        logger.info("Client finished initialization process");
    }

    private void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // todo
                        }
                    });

            ChannelFuture cf = bootstrap.connect(host, Integer.parseInt(port)).addListener( f -> {
                if(f.isSuccess()){
                    ChannelFuture channelFuture = (ChannelFuture) f;
                    this.channel = channelFuture.channel();
                } else {
                    logger.error("Failed to connect to Server, trying to reconnect again");
                    //reconect
                    reconnect();
                }
            });
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //todo
    public void reconnect() {

    }
}
