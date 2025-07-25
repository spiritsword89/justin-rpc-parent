package com.justin.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;

public class MessagePayload implements Serializable {
    //consumer id
    private String clientId;

    @JSONField(serializeUsing = MessageTypeSerializer.class, deserializeUsing = MessageTypeDeserializer.class)
    private MessageType messageType;
    private Object payload; //RpcRequest or RpcResponse

    public MessagePayload() {

    }

    public MessagePayload(RequestMessageBuilder builder) {
        this.clientId = builder.clientId;
        this.messageType = builder.messageType;

        if(messageType.equals(MessageType.CALL)) {
            //contruct RpcRequest
            this.payload = new RpcRequest(builder);
        } else if(messageType.equals(MessageType.RESPONSE)) {
            //construct RpcResponse
            this.payload = new RpcResponse(builder);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    //Builder Pattern
    public static class RequestMessageBuilder {
        private String clientId;
        private MessageType messageType;
        private String requestClientId;
        private String requestId;
        private String requestMethodSimpleName;
        private String requestClassName;
        private String[] paramsTypes;
        private Object[] params;
        private String returnValueType;
        private Object returnValue;

        public RequestMessageBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public RequestMessageBuilder setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
            return this;
        }

        public RequestMessageBuilder setReturnValueType(String returnValueType) {
            this.returnValueType = returnValueType;
            return this;
        }

        public RequestMessageBuilder setMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public RequestMessageBuilder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RequestMessageBuilder setRequestMethodName(String requestMethodName) {
            this.requestMethodSimpleName = requestMethodName;
            return this;
        }

        public RequestMessageBuilder setRequestedClassName(String requestedClassName) {
            this.requestClassName = requestedClassName;
            return this;
        }

        public RequestMessageBuilder setParamTypes(String[] paramTypes) {
            this.paramsTypes = paramTypes;
            return this;
        }

        public RequestMessageBuilder setParams(Object[] params) {
            this.params = params;
            return this;
        }

        public RequestMessageBuilder setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
            return this;
        }

        public MessagePayload build() {
            return new MessagePayload();
        }
    }

    public static class RpcRequest implements Serializable {
        // Producer id
        private String requestClientId;
        // unique UUID
        private String requestId;
        private String requestMethodSimpleName;
        private String requestClassName;
        private String returnValueType;
        private String[] paramsTypes;
        private Object[] params;

        public RpcRequest() {

        }

        public RpcRequest(RequestMessageBuilder builder) {
            this.requestClientId = builder.requestClientId;
            this.requestId = builder.requestId;
            this.requestMethodSimpleName = builder.requestMethodSimpleName;
            this.requestClassName = builder.requestClassName;
            this.paramsTypes = builder.paramsTypes;
            this.params = builder.params;
            this.returnValueType = builder.returnValueType;
        }

        public String getRequestClientId() {
            return requestClientId;
        }

        public void setRequestClientId(String requestClientId) {
            this.requestClientId = requestClientId;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getRequestMethodSimpleName() {
            return requestMethodSimpleName;
        }

        public void setRequestMethodSimpleName(String requestMethodSimpleName) {
            this.requestMethodSimpleName = requestMethodSimpleName;
        }

        public String getRequestClassName() {
            return requestClassName;
        }

        public void setRequestClassName(String requestClassName) {
            this.requestClassName = requestClassName;
        }

        public String getReturnValueType() {
            return returnValueType;
        }

        public void setReturnValueType(String returnValueType) {
            this.returnValueType = returnValueType;
        }

        public String[] getParamsTypes() {
            return paramsTypes;
        }

        public void setParamsTypes(String[] paramsTypes) {
            this.paramsTypes = paramsTypes;
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    public static class RpcResponse implements Serializable {
        private String requestId;
        private Object result;

        public RpcResponse() {

        }

        public RpcResponse (RequestMessageBuilder builder) {
            this.requestId = builder.requestId;
            this.result = builder.returnValue;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }
}
