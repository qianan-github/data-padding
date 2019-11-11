package com.qianan.padding.core.register;

import com.qianan.padding.core.register.adapter.ReturnValueListAdapter;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ReturnValueListAdapterRegistry {
    private final List<ReturnValueListAdapter> registrations = new ArrayList<>();

    public void addAdapter(ReturnValueListAdapter registration) {
        this.registrations.add(registration);
    }

    public void sort() {
        AnnotationAwareOrderComparator.sort(this.registrations);
    }

    public List adapter(Object o) {
        if (!CollectionUtils.isEmpty(registrations)) {
            for (ReturnValueListAdapter adapter : registrations) {
                if (adapter.supports(o)) {
                    return adapter.adapter(o);
                }
            }
        }
        throw new IllegalArgumentException("未配置任何返回值适配集合的组件");
    }
}
