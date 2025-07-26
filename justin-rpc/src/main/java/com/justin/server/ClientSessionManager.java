package com.justin.server;

import com.justin.model.MessagePayload;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {
    private static final Map<String, Channel> registerClients = new ConcurrentHashMap<>();
    private static final Map<String, MessagePayload> requestMap = new ConcurrentHashMap<>();

    public static void register(String clientId, Channel channel) {
        registerClients.put(clientId, channel);
    }

    public static boolean isClientRegistered(String clientId) {
        return registerClients.containsKey(clientId);
    }

    public static Channel getClientChannel(String clientId) {
        return registerClients.get(clientId);
    }

    public static void putRequest(MessagePayload messagePayload) {
        MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();
        requestMap.put(rpcRequest.getRequestId(), messagePayload);
    }

    public static MessagePayload getRequestClientMessage(String requestId) {
        return requestMap.get(requestId);
    }
}
