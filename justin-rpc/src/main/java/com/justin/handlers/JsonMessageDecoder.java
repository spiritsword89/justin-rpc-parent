package com.justin.handlers;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.justin.model.MessagePayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

public class JsonMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //1 byte = message type
        //4 bytes = length of message

        // return early
        if(byteBuf.readableBytes() < 5) {
            return;
        }

        // ByteBuffer

        // Heap ByteBuffer
        // Direct ByteBuffer

        // ByteBuf separates read and write pointers

        // Heap vs Direct
        // Heap backed by a regular java byte[]
        // Faster allocation
        // Bad: subject to GC


        //Pooled and Unpool
        //Pooled Buffer: reuse previously allocated memory chunks
        //Similar to threadpool

        //Frequency Heap allocation slows down the system
        // Netty default to use pooled direct buffer

        // all the chunks merged back to the intact message

        // The current buffer Bytebuf is not discarded,
        // when the next chunk of data arrives, it merges the new data with the previous leftover bytes.

        byteBuf.markReaderIndex();

        //Whenever the content is read, the readerIndex is increased
        byte messageType = byteBuf.readByte();
        int length = byteBuf.readInt();

        if(byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        MessagePayload messagePayload = JSON.parseObject(bytes, MessagePayload.class);

        switch (messageType) {
            case 1, 5:
                messagePayload.setPayload(null);
                break;
            case 2,3:
                JSONObject payload = (JSONObject) messagePayload.getPayload();
                MessagePayload.RpcRequest rpcRequest = payload.toJavaObject(MessagePayload.RpcRequest.class);
                messagePayload.setPayload(rpcRequest);
                break;
            case 4:
                JSONObject responsePayload = (JSONObject) messagePayload.getPayload();
                MessagePayload.RpcResponse rpcResponse = responsePayload.toJavaObject(MessagePayload.RpcResponse.class);
                messagePayload.setPayload(rpcResponse);
                break;
        }

        list.add(messagePayload);
    }
}
