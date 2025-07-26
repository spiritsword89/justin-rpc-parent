package com.justin.handlers;

import com.alibaba.fastjson.JSON;
import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonCallMessageEncoder extends MessageToByteEncoder<MessagePayload> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload, ByteBuf byteBuf) throws Exception {
        // The first byte is the message type
        byte type;

        if(messagePayload.getMessageType().equals(MessageType.REGISTER)) {
            type = 1;
        } else if (messagePayload.getMessageType().equals(MessageType.CALL)) {
            type = 2;
        } else if (messagePayload.getMessageType().equals(MessageType.FORWARD)) {
            type = 3;
        } else if (messagePayload.getMessageType().equals(MessageType.RESPONSE)) {
            type = 4;
        } else {
            type = 5;
        }

        byteBuf.writeByte(type);
        byte[] jsonBytes = JSON.toJSONBytes(messagePayload);

        // next 4 bytes = 1 int = the length of the message
        byteBuf.writeInt(jsonBytes.length);

        //the remaining is the message itself
        byteBuf.writeBytes(jsonBytes);
    }
}
