package com.justin.config;

import com.justin.client.RpcClient;
import com.justin.model.RemoteServiceFieldHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


//First, we need to find all classes that contain references to RPC service interfaces.
//Secondly, we collect these RPC interfaces into a Set, so we can remove duplicates
//Once we have this set, we iterate over each interface and register a RemoteServiceFactoryBean for it.

// This interface allows us to manually register BeanDefinition during the startup process, and more importantly
// The method registerBeanDefinitions provides access to an AnnotationMetadata parameter.
public class RpcServiceScanPostProcessor implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {

        String className = importingClassMetadata.getClassName();

        String basePackage = null;

        try {
            Class<?> applicationClass = Class.forName(className);
            basePackage = applicationClass.getPackage().getName();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        RemoteServiceScanner scanner = new RemoteServiceScanner(registry, false);
        scanner.doScan(new String[]{basePackage});
    }

    //    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//
//        Set<RemoteServiceFieldHolder> targetFields = new HashSet<>();
//
//        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
//
//        for (String beanDefinitionName : beanDefinitionNames) {
//            String beanClassName = registry.getBeanDefinition(beanDefinitionName).getBeanClassName();
//
//            if(beanClassName != null) {
//                try {
//                    Class<?> clazz = Class.forName(beanClassName);
//                    Field[] declaredFields = clazz.getDeclaredFields();
//
//                    for(Field declaredField : declaredFields) {
//                        declaredField.setAccessible(true);
//
//                        if(declaredField.isAnnotationPresent(AutoRemoteInjection.class)) {
//                            AutoRemoteInjection annotation = declaredField.getAnnotation(AutoRemoteInjection.class);
//                            RemoteServiceFieldHolder holder = new RemoteServiceFieldHolder(declaredField, annotation.requestClientId());
//
//                            if(annotation.fallbackClass() != null) {
//                                holder.setFallbackClass(annotation.fallbackClass());
//                            }
//
//                            targetFields.add(holder);
//                        }
//                    }
//
//                } catch (ClassNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//
//        for(RemoteServiceFieldHolder holder : targetFields) {
//            Class<?> targetRpcInterfaceClass = holder.getRemoteServiceField().getType();
//
//            if(RemoteService.class.isAssignableFrom(targetRpcInterfaceClass)) {
//                //Create a bean definition of RemoteServiceFactoryBean
//                AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RemoteServiceFactoryBean.class).getBeanDefinition();
//                //com.justin.common.rpc.booking.BookingDetailService
//                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(targetRpcInterfaceClass.getName());
//
//                beanDefinition.getPropertyValues().addPropertyValue("requestClientId", holder.getRequestClientId());
//                beanDefinition.getPropertyValues().addPropertyValue("rpcClient", new RuntimeBeanReference(RpcClient.class));
//
//                if(holder.getFallbackClass() != null) {
//                    beanDefinition.getPropertyValues().addPropertyValue("fallbackClass", holder.getFallbackClass());
//                }
//
//                registry.registerBeanDefinition(holder.getAlias(), beanDefinition);
//            }
//        }
//    }
}
