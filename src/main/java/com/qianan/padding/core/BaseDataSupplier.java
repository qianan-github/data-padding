package com.qianan.padding.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface BaseDataSupplier<T> {

    Map<Long, T> mapByIds(Set<Long> ids);

    default T findById(long id) {
        Set<Long> ids = new HashSet<>();
        ids.add(id);
        return mapByIds(ids).get(id);
    }
}
