# grid-helper

## 使用教程
### springBoot 中

如下所示, 仅需加入此配置类将拦截器配置到项目中, 即可在 controller 中使用了

```java

import com.guddqs.mybatis.SqlInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author wq
 * @date 2018/10/10
 * @description mybatis-grid-helper-config
 */

@Configuration
public class MybatisConfiguration {

    @Bean
    public SqlInterceptor sqlStatsInterceptor() {
        SqlInterceptor sqlStatsInterceptor = new SqlInterceptor();
        Properties properties = new Properties();
        properties.setProperty("dialect", "mysql");
        sqlStatsInterceptor.setProperties(properties);
        return sqlStatsInterceptor;
    }
}
```
### 常规ssm项目
如下: 将 bean 配置到 xml 中

```xml
<bean id="sqlInterceptor" class="com.guddqs.mybatis.SqlInterceptor">
	<property name="dialect" value="${jdbc.driverClassName}"/>
</bean>
<!-- sessionFactory配置mybatis拦截器, 省略其他配置 -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
	<property name="plugins">
		<array>
			<ref bean="sqlInterceptor" />
		</array>
	</property>
</bean>
```


## 使用示例
sqlMapper

```java

/**
 * 统一查找所有(带条件)
 * @param filters 条件 map
 * @return 多条结果
 * @throws Exception 异常拦截
 */
List<T> findAll(Map<String, Object> filters) throws Exception;
```
xml: 仅需正常查询即可

```xml
<select id="findAll" resultType="xxx.entity.xxx">
	select * from xxx 
</select>
```

Service: 这里 put 的2个 key 比较重要, 插件就是判断是否有这2个key, 才进行拦截

```java

public PageEntity<T> findAll(ParamVo paramVo, MapBean filter) throws Exception {
    if (filter == null) {
        filter = new MapBean();
    }
    filter.put("paramVo", paramVo);
    PageEntity<T> pageEntity = new PageEntity<>();
    filter.put("pageEntity", pageEntity);
    List<T> list = sqlMapper.findAll(filter);
    pageEntity.setData(list);
    return pageEntity;
}

@Override
public PageEntity<T> findAll(ParamVo paramVo) throws Exception {
    return findAll(paramVo, paramVo.getOther());
}
```
以上2个 查询方法, 适用于任何实体类, 可封装起来通过继承公用

controller: 这里使用了 RestController, 所有返回 Map 会自动转 json, 若无 RestController 注意转换成 json 返回

```java
public Map<String, Object> success(Object data) {
    Map<String, Object> map = new MapBean();
    map.put("success", true);
    map.put("code", ErrorCodes.SUCCESS.getCode());
    map.put("result", ErrorCodes.SUCCESS.getMsg());
    map.put("data", data);
    return map;
}

@PostMapping("/findUser")
public Map<String, Object> findAll(@RequestBody ParamVo paramVo) throws Exception {
    return success(xxService.findAll(paramVo));
}
```

以上是后端配置, 前端 Grid 使用方式请参考

[GitHub: my-jquery-grid](https://github.com/gudqs7/my-jquery-grid/tree/master/my-jquery-grid)
> 提示: 此前端组件尚不完善, 需配合 bootstrap 和 bootstrap-date-picker 和 awsome-font 使用

> 友情提示  
> 即使你不使用此前端组件, 你仍可接入此(分页,过滤,排序)插件, 因为代码量不大, 基本上参考 grid.js 里面的 ajax 请求, 即 ParamVo 类的结构, 轻量的修改此类代码即可





