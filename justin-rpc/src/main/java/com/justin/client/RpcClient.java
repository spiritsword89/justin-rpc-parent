package com.justin.client;

import com.justin.handlers.*;
import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Core component
// Two main tasks: 1. processing RpcRequest; 2. scann all RPC methods
public class RpcClient extends RemoteClientTemplate {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup worker = new NioEventLoopGroup();

    private ApplicationContext applicationContext;

    @Value("${justin.rpc.server.port}")
    private String port;

    @Value("${justin.rpc.client.host}")
    private String host;

    private Channel channel;


    public RpcClient() {

    }


    @PostConstruct
    public void initialize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();

        logger.info("Client finished initialization process");
    }

    @Override
    public void sendRequest(Object request, String requestId, CompletableFuture<MessagePayload.RpcResponse> future) {
        super.sendRequest(request, requestId, future);
        channel.writeAndFlush(request);
    }

    @Override
    public void sendResponse(Object message) {
        channel.writeAndFlush(message);
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
//                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
//                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new KryoCallMessageEncoder());
                            socketChannel.pipeline().addLast(new KryoMessageDecoder());
                            socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            socketChannel.pipeline().addLast(new ClientHeartbeatHandler());
                            socketChannel.pipeline().addLast(new RpcClientMessageHandler(RpcClient.this));
                        }
                    });

            bootstrap.connect(host, Integer.parseInt(port)).addListener( f -> {
                if(f.isSuccess()){
                    logger.info("Client ID: {} connected to server", getClientId());
                    ChannelFuture channelFuture = (ChannelFuture) f;
                    this.channel = channelFuture.channel();
                    //Send registration message to the server
                    sendRegistrationRequest();
                    channelFuture.channel().closeFuture().sync(); //blocking
                } else {
                    logger.error("Failed to connect to Server, trying to reconnect again");
                    //reconect
                    reconnect();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRegistrationRequest() {
        MessagePayload messagePayload = new MessagePayload
                .RequestMessageBuilder()
                .clientId(getClientId())
                .setMessageType(MessageType.REGISTER)
                .build();
        channel.writeAndFlush(messagePayload);
    }

    public void reconnect() {
        logger.info("Client {} is now being reconnected", getClientId());
        worker.schedule(this::connect, 5, TimeUnit.SECONDS);
    }
}
