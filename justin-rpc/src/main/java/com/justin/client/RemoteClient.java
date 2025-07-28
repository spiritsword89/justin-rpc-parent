package com.justin.client;

import com.justin.model.MessagePayload;

import java.util.concurrent.CompletableFuture;

public interface RemoteClient {
    public void sendRequest(Object message, String requestId, CompletableFuture<MessagePayload.RpcResponse> future);
    public void sendResponse(Object message);
    public void didCatchResponse(MessagePayload.RpcResponse response);
    public String getClientId();
}
