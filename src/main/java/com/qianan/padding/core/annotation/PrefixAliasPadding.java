package com.qianan.padding.core.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrefixAliasPadding {
    String alias() default "";
}