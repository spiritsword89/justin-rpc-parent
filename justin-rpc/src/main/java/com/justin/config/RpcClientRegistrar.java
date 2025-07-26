package com.justin.config;

import com.justin.client.RpcClient;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

// @Component, @Bean, @Configuration
// Retrieve the values of clientId and basePackages defined in the annotation EnableRpcClient
// Inject these values into RpcClient on Springboot startup

// Register any class as a Spring bean dynamically
// Scan packages for custom interfaces or annotations
// Usually working with @Import, ImportBeanDefinitionRegitrar implements are not a component itself.
// When spring sess the @Import annotation, it checks if the imported class implements one of special callback interfaces like:
// ImportSelector or ImportBeanDefinitionResgitrar
// if it does, Spring executes that logic during the bean definition phase before any beans are created.
public class RpcClientRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
       //register bean definition into Spring

        //it gives you a chance to manually register bean definition in early phase.

        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableRpcClient.class.getName());
        String clientId = (String) annotationAttributes.get("clientId");
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");

        if(clientId != null && basePackages != null){
            //create a bean definition for RpcClient
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcClient.class);

            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

            builder.addPropertyValue("clientId", clientId);
            builder.addPropertyValue("scanPackages", basePackages);
            builder.setScope(GenericBeanDefinition.SCOPE_SINGLETON);
            builder.setLazyInit(false);

            registry.registerBeanDefinition("rpcClient", beanDefinition);
        }
    }
}
