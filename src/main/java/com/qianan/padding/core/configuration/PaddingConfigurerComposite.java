package com.qianan.padding.core.configuration;

import com.qianan.padding.core.register.ReturnValueListAdapterRegistry;
import com.qianan.padding.core.register.ReturnValueUnpackRegistry;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class PaddingConfigurerComposite implements PaddingConfigurer {
    private final List<PaddingConfigurer> configurers = new ArrayList<>();

    public void addPaddingConfigurers(List<PaddingConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.configurers.addAll(configurers);
        }
    }

    @Override
    public void addReturnValueAdapter(ReturnValueListAdapterRegistry registry) {
        for (PaddingConfigurer configurer : configurers) {
            configurer.addReturnValueAdapter(registry);
        }
    }

    @Override
    public void addUnpackComponent(ReturnValueUnpackRegistry registry) {
        for (PaddingConfigurer configurer : configurers) {
            configurer.addUnpackComponent(registry);
        }
    }
}
