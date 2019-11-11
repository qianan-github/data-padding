package com.qianan.padding.core.register.adapter;

import java.util.List;

public class SingleObjectAdapter implements ReturnValueListAdapter {

    @Override
    public boolean supports(Object o) {
        return true;
    }

    @Override
    public List adapter(Object o) {
        return null;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
