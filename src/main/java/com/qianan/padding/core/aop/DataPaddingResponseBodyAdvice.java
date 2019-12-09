package com.qianan.padding.core.aop;

import com.qianan.padding.core.BaseDataSupplier;
import com.qianan.padding.core.annotation.DataPadding;
import com.qianan.padding.core.annotation.FieldPadding;
import com.qianan.padding.core.annotation.KeyPadding;
import com.qianan.padding.core.annotation.PrefixAliasPadding;
import com.qianan.padding.core.register.ReturnValueListAdapterRegistry;
import com.qianan.padding.core.register.ReturnValueUnpackRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ControllerAdvice
public class DataPaddingResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private Map<Class<?>, List<String>> activeNameCache = new ConcurrentHashMap<>();

    private final ReturnValueUnpackRegistry returnValueUnpackRegistry;
    private final ReturnValueListAdapterRegistry returnValueListAdapterRegistry;

    public DataPaddingResponseBodyAdvice(ReturnValueUnpackRegistry returnValueUnpackRegistry,
                                         ReturnValueListAdapterRegistry returnValueListAdapterRegistry) {
        this.returnValueUnpackRegistry = returnValueUnpackRegistry;
        this.returnValueListAdapterRegistry = returnValueListAdapterRegistry;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        AnnotatedElement annotatedElement = returnType.getAnnotatedElement();
        return super.supports(returnType, converterType) && AnnotationUtils.findAnnotation(annotatedElement, DataPadding.class) != null;
    }

    @Override
    protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType, MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
        long now = System.currentTimeMillis();
        //拆掉返回结果的最外层包装
        Object value = unpack(bodyContainer.getValue());
        //将返回结果适配为List
        List<?> list = returnValueListAdapterRegistry.adapter(value);
        dataPadding(list);
        log.info("Padding Takes |{}| Millis", System.currentTimeMillis() - now);
    }

    private Object unpack(Object returnValue) {
        return returnValueUnpackRegistry != null ? returnValueUnpackRegistry.unpack(returnValue) : returnValue;
    }

    protected Class<?> getItemClass(List<?> list) {
        return list.get(0).getClass();
    }


    private void dataPadding(List<?> list) {
        //空集直接返回
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //获取集合内元素的类型
        Class<?> clazz = getItemClass(list);
        //获取类中需要填充的字段
        List<String> activeName = getActivePropertiesName(clazz);

        //doWithFields本身就是从缓存中取得Fields
        ReflectionUtils.doWithFields(
                clazz,
                field -> {
                    //判断field是否集合类型，如果是，则嵌套填充数据
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        List items = new ArrayList();
                        for (Object item : list) {
                            items.addAll((Collection) field.get(item));
                        }
                        dataPadding(items);
                        return;
                    }
                    perFieldPadding(list, field, activeName);
                }
        );
    }

    private void perFieldPadding(List<?> list, Field field, List<String> activeName) {
        KeyPadding keyPadding = AnnotationUtils.findAnnotation(field, KeyPadding.class);
        if (keyPadding == null) {
            return;
        }
        //从spring上下文获取配置在注解中的处理类
        BaseDataSupplier<?> baseDataSupplier = applicationContext.getBean(keyPadding.cacheClass());

        MultiValueMap<Long, Object> multiValueMap = new LinkedMultiValueMap<>();
        field.setAccessible(true);
        list.forEach(item -> {
            Long id = (Long) ReflectionUtils.getField(field, item);
            multiValueMap.add(id, item);
        });

        Map<Long, ?> cacheMap = mapByIds(baseDataSupplier, multiValueMap.keySet());
        cacheMap.forEach((k, v) -> {
            List<?> cache = multiValueMap.get(k);
            if (!CollectionUtils.isEmpty(cache)) {
                cache.forEach(target -> copyPropertiesActive(v, target, activeName));
            }
        });
    }

    private Map<Long, ?> mapByIds(BaseDataSupplier<?> baseDataSupplier, Set<Long> ids) {
        Map<Long, ?> cacheMap = mapByIds0(baseDataSupplier, ids);
        if (cacheMap == null) {
            return new HashMap<>();
        }

        return cacheMap;
    }

    private Map<Long, ?> mapByIds0(BaseDataSupplier<?> baseDataSupplier, Set<Long> ids) {
        try {
            return baseDataSupplier.mapByIds(ids);
        } catch (Exception ex) {
            throw new RuntimeException("DataSupplier Provided An Exception | " + ex.getMessage() + " |");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DataPaddingResponseBodyAdvice.applicationContext = applicationContext;
    }

    private static void copyPropertiesActive(Object source, Object target, List<String> activeName) {

        Class<?> actualEditable = source.getClass();
        PrefixAliasPadding prefixAliasPadding = actualEditable.getDeclaredAnnotation(PrefixAliasPadding.class);
        String prefix = Objects.nonNull(prefixAliasPadding) ? prefixAliasPadding.alias() : "";

        PropertyDescriptor[] sourcePds = BeanUtils.getPropertyDescriptors(actualEditable);

        for (PropertyDescriptor sourcePd : sourcePds) {
            Method readMethod = sourcePd.getReadMethod();
            String sName = toCamel(prefix, sourcePd.getName());
            if (readMethod != null && activeName.contains(sName)) {
                PropertyDescriptor targetPd = BeanUtils.getPropertyDescriptor(target.getClass(), sName);
                if (targetPd != null) {
                    Method writeMethod = targetPd.getWriteMethod();
                    if (writeMethod != null) {
                        try {
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + sourcePd.getName() + "' from target1 to source", ex);
                        }
                    }
                }
            }
        }
    }

    private static String toCamel(String prefix, String name) {
        if (StringUtils.isEmpty(prefix)) {
            return name;
        }

        char c0;
        if ((c0 = name.charAt(0)) >= 'a' && c0 <= 'z') {
            return prefix + name.replaceFirst(String.valueOf(c0), String.valueOf((char)(c0 - 32)));
        }
        return name;
    }

    private List<String> getActivePropertiesName(Class<?> clazz) {
        return activeNameCache.computeIfAbsent(
                clazz,
                cls -> {
                    List<String> list = new ArrayList<>();
                    ReflectionUtils.doWithFields(
                            cls,
                            field -> list.add(field.getName()),
                            field -> AnnotationUtils.findAnnotation(field, FieldPadding.class) != null);
                    return list;
                });
    }
}
