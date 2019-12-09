# data-padding
数据填充，提供主键id，填充需要显示的字段

## 如何贡献和帮助
欢迎提出修改建议和优化建议使这个项目更加完善

## 前提条件
- JDK 1.8

## 功能特性:
- [x] 字段填充
- [x] 集合嵌套填充
- [x] 拆除外层统一包装
- [x] 自定义集合类型出参
- [ ] ~~是否开启集合嵌套填充~~
- [ ] FieldPadding控制集合是否填充
- [x] PrefixAliasPadding注解

## Quick Start
注意：当前版本1.0-SNAPSHOT
```xml
<!--在pom.xml中添加依赖-->
<dependency>
    <groupId>com.qianan</groupId>
    <artifactId>padding-spring-boot-starter</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### 配置
```java
@Configuration
public class AppConfiguration implements PaddingConfigurer {
    @Override
    public void addUnpackComponent(ReturnValueUnpackRegistry registry) {
        registry.addAdapter(returnValue -> returnValue instanceof RestResponse ? ((RestResponse) returnValue).getData() : returnValue);
    }

    @Override
    public void addReturnValueAdapter(ReturnValueListAdapterRegistry registry) {
        registry.addAdapter(new ReturnValueListAdapter() {
            @Override
            public boolean supports(Object o) {
                //自定义的集合出参，ListOutput中定义了一个属性`private List<T> list`
                return o instanceof ListOutput;
            }

            @Override
            public List adapter(Object o) {
                return ((ListOutput) o).getList();
            }

            @Override
            public int getOrder() {
                //排序规则，数值越小越先执行
                return 1;
            }
        });
    }
}

```
### 控制层打上注解
```java
@RestController
@RequestMapping("/test")
public class TestController {
    
    @DataPadding
    @RequestMapping("/list")
    public RestResponse<ListOutput<UserOutput>> list() {
        
    }
}
```
### 实现接口，提供数据查询结果
```java
@DataSupplier
public class UserDataSupplier implements BaseDataSupplier<UserEntity> {

    @Override
    public Map<Long, UserEntity> mapByIds(Set<Long> ids) {
        //...
        return null;
    }
}
```
### 前端对象配置
```java
@Data
public class UserOutput {
    @KeyPadding(cacheClass = UserDataSupplier.class)
    private long userId;

    //该注解表示需要填充的字段
    @FieldPadding
    private String userName;

    //集合默认填充
    private List<BookOutput> books;
}
```
