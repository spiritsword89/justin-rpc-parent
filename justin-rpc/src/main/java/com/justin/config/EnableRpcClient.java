package com.justin.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RpcClientRegistrar.class)
public @interface EnableRpcClient {
    String clientId() default "";
    String[] basePackages() default {};
}
