package com.qianan.padding.core.register;

import com.qianan.padding.core.register.unpack.ReturnValueUnpackHandler;

public class ReturnValueUnpackRegistry {
    private ReturnValueUnpackHandler returnValueUnpackHandler;

    public void addUnpack(ReturnValueUnpackHandler returnValueUnpackHandler) {
        this.returnValueUnpackHandler = returnValueUnpackHandler;
    }

    public Object unpack(Object returnValue) {
        return returnValueUnpackHandler != null ? returnValueUnpackHandler.unpack(returnValue) : returnValue;
    }
}
