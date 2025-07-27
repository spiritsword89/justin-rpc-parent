package com.justin.handlers;

import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if(event.state() == IdleState.WRITER_IDLE) {
                System.out.println("Rpc Client starts sending heart beat message");
                MessagePayload messagePayload = new MessagePayload.RequestMessageBuilder().setMessageType(MessageType.HEART_BEAT).build();
                ctx.writeAndFlush(messagePayload);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
