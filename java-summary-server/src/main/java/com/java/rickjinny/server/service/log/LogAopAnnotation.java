package com.java.rickjinny.server.service.log;

import java.lang.annotation.*;

/**
 * Spring AOP 的触发点 (注解)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented()
public @interface LogAopAnnotation {
    String value();
}
