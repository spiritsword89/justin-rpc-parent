package com.justin.handlers;

import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import com.justin.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Core handler to process the message
public class RpcServerMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {
    private final Logger logger = LoggerFactory.getLogger(RpcServerMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessagePayload messagePayload) throws Exception {
        // RpcRequest and RpcResponse

        // 1. forward the message
        // 2. Register client
        // 3. Return the response

        MessageType messageType = messagePayload.getMessageType();

        if(messageType.equals(MessageType.REGISTER)) {
            registerClientIntoSession(messagePayload, ctx.channel());
        }

        if (messageType.equals(MessageType.CALL)) {
            // Find out the channel of the producer
            MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();

            // Consumer calls itself
            if(messagePayload.getClientId().equals(rpcRequest.getRequestClientId())) {
                throw new RuntimeException("Client Id and Request Client Id are the same.");
            }

            // Looking for producer channel
            Channel channel = ClientSessionManager.getClientChannel(rpcRequest.getRequestClientId());

            if(channel == null) {
                throw new RuntimeException("Channel does not exist");
            }

            forwardRequestToClient(messagePayload, channel);
        }

        if(messageType.equals(MessageType.RESPONSE)) {
            //When the response is returned, we need to send it back to the consumer.
            returnResponseToClient(messagePayload);
        }
    }

    private void returnResponseToClient(MessagePayload messagePayload) {
        MessagePayload.RpcResponse rpcResponse = (MessagePayload.RpcResponse) messagePayload.getPayload();
        String requestId = rpcResponse.getRequestId();
        MessagePayload requestMessage = ClientSessionManager.getRequestClientMessage(requestId);
        Channel channel = ClientSessionManager.getClientChannel(requestMessage.getClientId());
        channel.writeAndFlush(messagePayload);
    }

    private void forwardRequestToClient(MessagePayload messagePayload, Channel channel) {
        ClientSessionManager.putRequest(messagePayload);
        messagePayload.setMessageType(MessageType.FORWARD);
        channel.writeAndFlush(messagePayload);
    }

    private void registerClientIntoSession(MessagePayload messagePayload, Channel channel) {
        logger.info("Client id {} is now registering with Netty Server",  messagePayload.getClientId());
        ClientSessionManager.register(messagePayload.getClientId(), channel);
    }
}
