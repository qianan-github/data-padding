package com.qianan.padding.core.configuration;

import com.qianan.padding.core.register.ReturnValueListAdapterRegistry;
import com.qianan.padding.core.register.ReturnValueUnpackRegistry;
import com.qianan.padding.core.register.adapter.CollectionAdapter;
import com.qianan.padding.core.register.adapter.SingleObjectAdapter;
import org.springframework.core.Ordered;

public interface PaddingConfigurer {

    /**
     * 添加返回值适配器
     * 将返回结果适配为List
     * 默认提供Collection{@link CollectionAdapter}和Object{@link SingleObjectAdapter}适配
     * 注意排序{@link Ordered}，值越小越靠前越会被首先适配
     * */
    default void addReturnValueAdapter(ReturnValueListAdapterRegistry registry) {}

    /**
     * 拆掉返回结果的最外层包装
     * 如：RestResponse{code,msg,data} -> data
     * */
    default void addUnpackComponent(ReturnValueUnpackRegistry registry) {}
}
