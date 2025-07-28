package com.justin.config;

import com.justin.client.RpcClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;

// 1. we can get all the bean definitions.
// 2. Iterate over these bean definitions (Bean Definition Name)
// 3. Create Class object using the Bean Definition Class Name
// 4. We can get all declared fields, we need to check if the field has AutoRemoteInjection Annotation, if yes, then put this field into a set.
// 5. Iterate the set, for each target interface, we can create a FactoryBean (implementation class) and set the BeanClass as this target RPC interface.
public class PostStartupProcessor implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);

            Field[] declaredFields = bean.getClass().getDeclaredFields();

            for (Field field : declaredFields) {
                boolean annotationPresent = field.isAnnotationPresent(AutoRemoteInjection.class);

                if(annotationPresent){
                    AutoRemoteInjection annotation = field.getAnnotation(AutoRemoteInjection.class);
                    String requestClientId = annotation.requestClientId();
                    //Class object of the RPC interface
                    Class<?> fieldType = field.getType();
                    if(RemoteService.class.isAssignableFrom(fieldType)){
                        // Generate proxy and inject to this field
                        RpcClient rpcClient = applicationContext.getBean(RpcClient.class);
                        Object proxy = rpcClient.generate(fieldType, requestClientId);

                        field.setAccessible(true);
                        try {
                            field.set(bean, proxy);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
