package com.justin.client;

import com.justin.config.MarkAsRpc;
import com.justin.config.RemoteService;
import com.justin.model.RpcMethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Method;
import java.util.*;

// Core component
// Two main tasks: 1. processing RpcRequest; 2. scann all RPC methods
public class RpcClient implements SmartInitializingSingleton {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup worker = new NioEventLoopGroup();

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

    public RpcClient() {

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

    private void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // todo
                        }
                    });

            ChannelFuture cf = bootstrap.connect(host, Integer.parseInt(port)).addListener( f -> {
                if(f.isSuccess()){
                    ChannelFuture channelFuture = (ChannelFuture) f;
                    this.channel = channelFuture.channel();
                } else {
                    logger.error("Failed to connect to Server, trying to reconnect again");
                    //reconect
                    reconnect();
                }
            });
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //todo
    public void reconnect() {

    }

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
}
