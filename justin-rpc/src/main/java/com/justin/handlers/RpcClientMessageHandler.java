package com.justin.handlers;

import com.justin.client.RpcClient;
import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// RpcClient Message Handler to process the inbound message
public class RpcClientMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {

    // When the RPC call arrives at the client side
    // Check if the message is Forward (Rpc Call) or Response (Rpc Response)
    private RpcClient rpcClient;

    public RpcClientMessageHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload) throws Exception {
        if(messagePayload.getMessageType() == MessageType.FORWARD){
            processRequestAndGenerateResponse(messagePayload);
        } else if (messagePayload.getMessageType() == MessageType.RESPONSE){
            completeRequest(messagePayload);
        }
    }

    private void completeRequest(MessagePayload messagePayload) {

    }

    private void processRequestAndGenerateResponse(MessagePayload messagePayload) {
        // When the message arrives, it needs to be processed.
        // Process logic involves the PRC method calling.
        rpcClient.processRequest(messagePayload);
    }
}
