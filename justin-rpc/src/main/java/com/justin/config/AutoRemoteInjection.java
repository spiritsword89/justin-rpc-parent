package com.justin.config;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoRemoteInjection {
    String requestClientId() default "";
    Class<?> fallbackClass() default Void.class;
}
