package com.justin.config;

import com.justin.client.RpcClient;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RpcClient.class)
public @interface EnableRpcClient {
    String clientId() default "";
    String[] basePackages() default {};
}
