package com.justin.config;

import com.justin.client.RemoteClient;
import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


// PaymentDetailService implements RemoteService
// UserDetailService implements RemoteService

// In one of the client, we have reference to both PaymentDetailService and UserDetailService For RPC call.

//Where, when, and how we can create this RemoteServiceFactoryBean
// BeanDefinitionRegistryPostProcessor interface
public class RemoteServiceFactoryBean<T> implements FactoryBean<T> {

    private String requestClientId;

    private Class<T> rpcInterfaceClass;

    private Class<? extends T> fallbackClass;

    private RemoteClient rpcClient;

    //constructor
    public RemoteServiceFactoryBean(Class<T> rpcInterfaceClass) {
        this.rpcInterfaceClass = rpcInterfaceClass;
    }

    public Class<? extends T> getFallbackClass() {
        return fallbackClass;
    }

    public void setFallbackClass(Class<? extends T> fallbackClass) {
        this.fallbackClass = fallbackClass;
    }

    public String getRequestClientId() {
        return requestClientId;
    }

    public void setRequestClientId(String requestClientId) {
        this.requestClientId = requestClientId;
    }

    public Class<T> getRpcInterfaceClass() {
        return rpcInterfaceClass;
    }

    public void setRpcInterfaceClass(Class<T> rpcInterfaceClass) {
        this.rpcInterfaceClass = rpcInterfaceClass;
    }

    public RemoteClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RemoteClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @SuppressWarnings("all")
    @Override
    public T getObject() throws Exception {
        // return the proxy of the RPC interface
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ rpcInterfaceClass }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if (methodName.equals("toString")) return "Proxy[" + this.getClass().getSimpleName() + "]";
                if (methodName.equals("hashCode")) return System.identityHashCode(proxy);
                if (methodName.equals("equals")) return proxy == args[0];

                Class<?>[] parameterTypes = method.getParameterTypes();

                String[] paramTypes = new String[parameterTypes.length];

                for(int i = 0; i < parameterTypes.length; i++) {
                    paramTypes[i] = parameterTypes[i].getSimpleName(); //get simple name
                }

                String requestId = UUID.randomUUID().toString();

                Class<?> remoteRpcInterface = getRemoteRpcInterface(rpcInterfaceClass);

                if(remoteRpcInterface == null) {
                    return triggerFallback(method, args);
                }

                MessagePayload messagePayload = new MessagePayload.RequestMessageBuilder()
                        .clientId(rpcClient.getClientId())
                        .setMessageType(MessageType.CALL)
                        .setRequestId(requestId)
                        .setRequestClientId(requestClientId)
                        .setParamTypes(paramTypes)
                        .setParams(args)
                        .setRequestMethodName(methodName)
                        .setReturnValueType(method.getReturnType().getSimpleName())
                        .setRequestedClassName(remoteRpcInterface.getName()).build();

                CompletableFuture<MessagePayload.RpcResponse> future = new CompletableFuture<>();
                rpcClient.sendRequest(messagePayload, requestId, future);
                //wait for the result to come back
                try {
                    MessagePayload.RpcResponse rpcResponse = future.get(5, TimeUnit.SECONDS); // blocking and waiting
                    rpcClient.didCatchResponse(rpcResponse);
                    return rpcResponse.getResult();
                }catch (Exception e) {
                    // Fallback mechanism is going to be added here.
                    // The fallback needs to do the following:
                    // return a default value
                    // executes an alternative method
                    // logs the error and continues
                    return triggerFallback(method, args);
                }
            }
        });
    }

    private Class<?> getRemoteRpcInterface(Class<?> targetInterface) {
        // Find out the second level interface which directly extends from the RemoteService
        for(Class<?> interfaceClass : targetInterface.getInterfaces()) {
            if(interfaceClass.equals(RemoteService.class)) {
                return targetInterface;
            }

            Class<?> found = getRemoteRpcInterface(interfaceClass);

            if(found != null) {
                return found;
            }
        }

        return null;
    }

    private Object triggerFallback(Method method, Object[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(fallbackClass != null) {
            Object fallbackBean = fallbackClass.getConstructor().newInstance();
            Method fallbackMethod = fallbackClass.getMethod(method.getName(), method.getParameterTypes());
            return fallbackMethod.invoke(fallbackBean, args);
        }
        return "Timeout.";
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterfaceClass;
    }
}
