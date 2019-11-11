package com.qianan.padding.core.register.unpack;

@FunctionalInterface
public interface ReturnValueUnpackHandler {
    Object unpack(Object returnValue);
}
