package com.justin.config;

import com.justin.client.RpcClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class RemoteServiceScanner extends ClassPathBeanDefinitionScanner {
    public RemoteServiceScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);

        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                return (classMetadata.isInterface() && annotationMetadata.hasAnnotation(AutoRemoteInjection.class.getName()));
            }
        });
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);

        // This set contains all customized RPC interfaces with AutoRemoteInjection annotated.
        for(BeanDefinitionHolder holder : beanDefinitionHolders){

            //Bean Class = MyUserBookingDetailService.class
            BeanDefinition beanDefinition = holder.getBeanDefinition();

            if(beanDefinition instanceof ScannedGenericBeanDefinition scannedBd) {
                AnnotationMetadata metadata = scannedBd.getMetadata();
                Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(AutoRemoteInjection.class.getName());
                // This is producer id
                String requestClientId = annotationAttributes.get("requestClientId").toString();
                Class<?> fallbackClass = (Class<?>) annotationAttributes.get("fallbackClass");

                //com.justin_demo.rpc.MyUserBookingDetailService
                String beanClassName = scannedBd.getBeanClassName();

                scannedBd.setBeanClass(RemoteServiceFactoryBean.class);
                scannedBd.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
                scannedBd.getPropertyValues().addPropertyValue("requestClientId", requestClientId);
                scannedBd.getPropertyValues().addPropertyValue("rpcClient", new RuntimeBeanReference(RpcClient.class));

                if(fallbackClass != null){
                    scannedBd.getPropertyValues().addPropertyValue("fallbackClass", fallbackClass);
                }
            }
        }

        return beanDefinitionHolders;
    }
}
