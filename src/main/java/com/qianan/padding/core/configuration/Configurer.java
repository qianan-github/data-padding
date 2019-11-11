package com.qianan.padding.core.configuration;

import com.qianan.padding.core.register.ReturnValueListAdapterRegistry;
import com.qianan.padding.core.register.ReturnValueUnpackRegistry;
import com.qianan.padding.core.register.adapter.CollectionAdapter;
import com.qianan.padding.core.register.adapter.SingleObjectAdapter;
import com.qianan.padding.core.aop.DataPaddingResponseBodyAdvice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import(DataPaddingResponseBodyAdvice.class)
public class Configurer {

    @Bean
    public ReturnValueListAdapterRegistry returnValueListAdapterRegistry() {
        ReturnValueListAdapterRegistry adapterRegistry = new ReturnValueListAdapterRegistry();
        adapterRegistry.addAdapter(new CollectionAdapter());
        adapterRegistry.addAdapter(new SingleObjectAdapter());
        return adapterRegistry;
    }

    @Bean
    public ReturnValueUnpackRegistry returnValueUnpackRegistry() {
        return new ReturnValueUnpackRegistry();
    }

    @Bean
    public PaddingConfigurerComposite paddingConfigurerComposite(List<PaddingConfigurer> configurers) {
        PaddingConfigurerComposite configurerComposite = new PaddingConfigurerComposite();
        configurerComposite.addPaddingConfigurers(configurers);
        addReturnValueAdapter(configurerComposite);
        configurerComposite.addUnpackComponent(returnValueUnpackRegistry());
        return configurerComposite;
    }

    private void addReturnValueAdapter(PaddingConfigurerComposite configurerComposite) {
        ReturnValueListAdapterRegistry adapterRegistry = returnValueListAdapterRegistry();
        configurerComposite.addReturnValueAdapter(adapterRegistry);
        adapterRegistry.sort();
    }

}
