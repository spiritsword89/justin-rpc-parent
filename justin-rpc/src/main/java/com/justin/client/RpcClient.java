package com.justin.client;

import com.justin.config.MarkAsRpc;
import com.justin.config.RemoteService;
import com.justin.handlers.ClientHeartbeatHandler;
import com.justin.handlers.JsonCallMessageEncoder;
import com.justin.handlers.JsonMessageDecoder;
import com.justin.handlers.RpcClientMessageHandler;
import com.justin.model.MessagePayload;
import com.justin.model.MessageType;
import com.justin.model.RpcMethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// Core component
// Two main tasks: 1. processing RpcRequest; 2. scann all RPC methods
public class RpcClient implements SmartInitializingSingleton, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup worker = new NioEventLoopGroup();

    private ApplicationContext applicationContext;

    @Value("${justin.rpc.server.port}")
    private String port;

    @Value("${justin.rpc.client.host}")
    private String host;

    private String clientId;

    private String[] scanPackages;

    private Channel channel;

    //Key = full class name of the RPC implementation // Map (key: method Id, value = RpcMethodDescriptor)
    private Map<String, Map<String, RpcMethodDescriptor>> methodsHashMap = new HashMap<>();

    private Map<String, Method> classMethodsMap = new HashMap<>();

    private Map<String, CompletableFuture<MessagePayload.RpcResponse>> requestMap = new ConcurrentHashMap<>();

    public RpcClient() {

    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    @PostConstruct
    public void initialize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();

        logger.info("Client finished initialization process");
    }

    public void completeRequest(MessagePayload.RpcResponse rpcResponse) {
        String requestId = rpcResponse.getRequestId();

        CompletableFuture<MessagePayload.RpcResponse> future = requestMap.get(requestId);

        future.complete(rpcResponse);
    }

    // Process the RPC request from the consumer
    public void processRequest(MessagePayload messagePayload) {
        MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();

        // Rpc interface full class name
        String requestClassName = rpcRequest.getRequestClassName();

        String methodName = rpcRequest.getRequestMethodSimpleName();
        String[] paramsTypes = rpcRequest.getParamsTypes();
        Object[] params = rpcRequest.getParams();
        String returnValueType = rpcRequest.getReturnValueType();

        //Generate Method Id unique
        String methodId = RpcMethodDescriptor.generateMethodId(methodName, paramsTypes.length, paramsTypes, returnValueType);

        // find out the request class
        // In the RPC request call, the message carries the RPC interface (BookingDetailService)
        // Using the interface to find any implementation (Find BookingDetailServiceImpl)

        Class<?> requestClass = null;
        try {
            requestClass = Class.forName(requestClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // We need to use this Class object to find out the implementation.
        Map<String, ?> beansOfType = applicationContext.getBeansOfType(requestClass);

        if(beansOfType.isEmpty()) {
            throw new RuntimeException("Class of implementation is not found");
        }

        // Find two implementations
        if(beansOfType.size() > 1) {
            throw new RuntimeException("Multiple implementations of request class found");
        }

        Object requestedClassBean = beansOfType.values().iterator().next();

        String fullRequestClassBeanName = requestedClassBean.getClass().getName();

        // Key: Method ID
        Map<String, RpcMethodDescriptor> methodDescriptorMap = methodsHashMap.get(fullRequestClassBeanName);

        RpcMethodDescriptor rpcMethodDescriptor = methodDescriptorMap.get(methodId);

        if(rpcMethodDescriptor == null) {
            throw new RuntimeException("No RPC method supported");
        }

        // Very strict validation process shall be considered.
        // How about security? What if the method requires token check?
        if(rpcMethodDescriptor.getMethodName().equals(rpcRequest.getRequestMethodSimpleName())
                && rpcMethodDescriptor.getNumOfParams() == paramsTypes.length) {
            Method method = classMethodsMap.get(methodId);

            try {
                Object result = method.invoke(requestedClassBean, params);

                // Write the result back to the consumer
                // Build a RpcResponse

                MessagePayload responseMessage = new MessagePayload.RequestMessageBuilder()
                        .setRequestId(rpcRequest.getRequestId())
                        .setMessageType(MessageType.RESPONSE)
                        .setReturnValue(result).build();

                channel.writeAndFlush(responseMessage);

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("all")
    public <T> T generate(Class<T> proxyTarget, String requestClientId) {
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{proxyTarget}, new InvocationHandler() {
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

                MessagePayload messagePayload = new MessagePayload.RequestMessageBuilder()
                        .clientId(clientId)
                        .setMessageType(MessageType.CALL)
                        .setRequestId(requestId)
                        .setRequestClientId(requestClientId)
                        .setParamTypes(paramTypes)
                        .setParams(args)
                        .setRequestMethodName(methodName)
                        .setReturnValueType(method.getReturnType().getSimpleName())
                        .setRequestedClassName(proxyTarget.getName()).build();

                CompletableFuture<MessagePayload.RpcResponse> future = new CompletableFuture<>();
                requestMap.put(requestId, future);
                channel.writeAndFlush(messagePayload);
                //wait for the result to come back
                try {
                    MessagePayload.RpcResponse rpcResponse = future.get(5, TimeUnit.SECONDS); // blocking and waiting
                    requestMap.remove(requestId);
                    return rpcResponse.getResult();
                }catch (Exception e) {
                    // Fallback mechanism is going to be added here.
                    return "Timeout!";
                }
            }
        });
    }

    private void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            socketChannel.pipeline().addLast(new ClientHeartbeatHandler());
                            socketChannel.pipeline().addLast(new RpcClientMessageHandler(RpcClient.this));
                        }
                    });

            bootstrap.connect(host, Integer.parseInt(port)).addListener( f -> {
                if(f.isSuccess()){
                    logger.info("Client ID: {} connected to server", clientId);
                    ChannelFuture channelFuture = (ChannelFuture) f;
                    this.channel = channelFuture.channel();
                    //Send registration message to the server
                    sendRegistrationRequest();
                    channelFuture.channel().closeFuture().sync(); //blocking
                } else {
                    logger.error("Failed to connect to Server, trying to reconnect again");
                    //reconect
                    reconnect();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRegistrationRequest() {
        MessagePayload messagePayload = new MessagePayload
                .RequestMessageBuilder()
                .clientId(clientId)
                .setMessageType(MessageType.REGISTER)
                .build();
        channel.writeAndFlush(messagePayload);
    }

    public void reconnect() {
        logger.info("Client {} is now being reconnected", clientId);
        worker.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    // RPC method scanning process
    @Override
    public void afterSingletonsInstantiated() {
        // right timing to scann all RPC methods here
        // RPC methods must be defined in interfaces extending from RemoteService
        // RPC methods have MarkAsRpc annotated
        System.out.println("afterSingletonsInstantiated started");

        List<Class<?>> classList = new ArrayList<>();
        for(String scanPackage : scanPackages){
            scanPackage = scanPackage.replaceAll("\\.", "/");
            ClassPathScanningCandidateComponentProvider scanProvider = new ClassPathScanningCandidateComponentProvider(false);
            scanProvider.addIncludeFilter(new AssignableTypeFilter(Object.class));
            Set<BeanDefinition> candidateComponents = scanProvider.findCandidateComponents(scanPackage);

            for(BeanDefinition beanDefinition : candidateComponents){
                try {
                    Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());

                    if(RemoteService.class.isAssignableFrom(aClass)) {
                        //qualified
                        classList.add(aClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for(Class<?> clazz : classList){
            //qualified RPC implemented class
            Method[] declaredMethods = clazz.getDeclaredMethods();

            for(Method declaredMethod : declaredMethods){
                if(declaredMethod.isAnnotationPresent(MarkAsRpc.class)) {
                    // This is RPC method!

                    // 1. We need to extract the meta of the method
                    RpcMethodDescriptor md = RpcMethodDescriptor.build(declaredMethod);

                    // 2. We need to save the meta info
                    methodsHashMap.computeIfAbsent(clazz.getName(), k -> new HashMap<>()).put(md.getMethodId(), md);

                    // 3. We need to save this method
                    classMethodsMap.put(md.getMethodId(), declaredMethod);
                    //We need two maps
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
