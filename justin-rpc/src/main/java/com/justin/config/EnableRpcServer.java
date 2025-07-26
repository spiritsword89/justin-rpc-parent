package com.justin.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RpcServerConfiguration.class) //Import a configuration class
public @interface EnableRpcServer {
}
