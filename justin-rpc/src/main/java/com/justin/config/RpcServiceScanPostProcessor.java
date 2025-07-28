package com.justin.config;

import com.justin.client.RpcClient;
import com.justin.model.RemoteServiceFieldHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


//First, we need to find all classes that contain references to RPC service interfaces.
//Secondly, we collect these RPC interfaces into a Set, so we can remove duplicates
//Once we have this set, we iterate over each interface and register a RemoteServiceFactoryBean for it.
public class RpcServiceScanPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        Set<RemoteServiceFieldHolder> targetFields = new HashSet<>();

        String[] beanDefinitionNames = registry.getBeanDefinitionNames();

        for (String beanDefinitionName : beanDefinitionNames) {
            String beanClassName = registry.getBeanDefinition(beanDefinitionName).getBeanClassName();

            if(beanClassName != null) {
                try {
                    Class<?> clazz = Class.forName(beanClassName);
                    Field[] declaredFields = clazz.getDeclaredFields();

                    for(Field declaredField : declaredFields) {
                        declaredField.setAccessible(true);

                        if(declaredField.isAnnotationPresent(AutoRemoteInjection.class)) {
                            AutoRemoteInjection annotation = declaredField.getAnnotation(AutoRemoteInjection.class);
                            RemoteServiceFieldHolder holder = new RemoteServiceFieldHolder(declaredField, annotation.requestClientId());
                            targetFields.add(holder);
                        }
                    }

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for(RemoteServiceFieldHolder holder : targetFields) {
            Class<?> targetRpcInterfaceClass = holder.getRemoteServiceField().getType();

            if(RemoteService.class.isAssignableFrom(targetRpcInterfaceClass)) {
                //Create a bean definition of RemoteServiceFactoryBean
                AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RemoteServiceFactoryBean.class).getBeanDefinition();
                //com.justin.common.rpc.booking.BookingDetailService
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(targetRpcInterfaceClass.getName());

                beanDefinition.getPropertyValues().addPropertyValue("requestClientId", holder.getRequestClientId());
                beanDefinition.getPropertyValues().addPropertyValue("rpcClient", new RuntimeBeanReference(RpcClient.class));

                registry.registerBeanDefinition(holder.getAlias(), beanDefinition);
            }
        }
    }
}
