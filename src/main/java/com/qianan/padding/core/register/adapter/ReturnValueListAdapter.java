package com.qianan.padding.core.register.adapter;

import org.springframework.core.Ordered;

import java.util.List;

public interface ReturnValueListAdapter extends Ordered {
    boolean supports(Object o);

    List adapter(Object o);
}
