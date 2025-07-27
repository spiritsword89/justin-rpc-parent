package com.justin.handlers;

import com.justin.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    public ServerHeartbeatHandler() {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event){
            if(event.state() == IdleState.READER_IDLE){
                System.out.println("Reader Idle triggered");
                Channel channel = ctx.channel();
                //Next step: we need to clear the ClientSessionManager, we need client id
                String clientId = channel.attr(AttributeKey.valueOf("clientId")).get().toString();
                ClientSessionManager.clearByClientId(clientId);
                channel.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
