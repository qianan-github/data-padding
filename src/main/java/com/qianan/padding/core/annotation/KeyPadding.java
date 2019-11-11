package com.qianan.padding.core.annotation;

import com.qianan.padding.core.BaseDataSupplier;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KeyPadding {
    Class<? extends BaseDataSupplier> cacheClass();
}
