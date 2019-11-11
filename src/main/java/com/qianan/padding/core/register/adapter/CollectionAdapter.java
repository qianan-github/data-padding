package com.qianan.padding.core.register.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionAdapter implements ReturnValueListAdapter {
    @Override
    public boolean supports(Object o) {
        return o instanceof Collection;
    }

    @Override
    public List adapter(Object o) {
        Collection c = (Collection) o;
        return new ArrayList(c);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
