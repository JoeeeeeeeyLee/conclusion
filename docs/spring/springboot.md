[TOC]

# Spring Boot

Spring Boot是一个基于Spring的套件，它帮我们预组装了Spring的一系列组件，以便以尽可能少的代码和配置来开发基于Spring的Java应用程序

## 一. 第一个Spring Boot应用

`application.yml`或者`application.properties`为配置文件的名字，使用`.yml`冒号后面要加空格

在配置文件中我们经常使用如下的格式对某个key进行配置

```yaml
app:
  db:
    host: ${DB_HOST:localhost}
```

`${DB_HOST:localhost}`的意思是，首先从环境变量中找`DB_HOST`，如果环境变量定义了，就使用定义的值，否则，使用默认值`localhost`

这是Spring Boot的logback配置文件名称（也可以使用`logback.xml`），一个标准的写法如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml" />

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>utf8</charset>
        </encoder>
    </appender>

    <appender name="APP_LOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>utf8</charset>
        </encoder>
          <file>app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy">
            <maxIndex>1</maxIndex>
            <fileNamePattern>app.log.%i</fileNamePattern>
        </rollingPolicy>
        <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
            <MaxFileSize>1MB</MaxFileSize>
        </triggeringPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="APP_LOG" />
    </root>
</configuration>
```

它主要通过`<include resource="..." />`引入了Spring Boot的一个缺省配置，这样我们就可以引用类似`${CONSOLE_LOG_PATTERN}`这样的变量。上述配置定义了一个控制台输出和文件输出，可根据需要修改

Spring Boot要求`main()`方法所在的启动类必须放到根package下，命名不作要求

```java
//这一个注解就相当于启动了自动配置和自动扫描
@SpringBootApplication
publuc class Application{
    public static void main(String[] args){
        SpringApplication.run(Application.class,args);
    }
}
```

`pom.xml`的内容如下

```xml
<project ...>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.0.RELEASE</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.itranswarp.learnjava</groupId>
    <artifactId>springboot-hello</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <java.version>11</java.version>
        <pebble.version>3.1.2</pebble.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <!-- 集成Pebble View -->
        <dependency>
            <groupId>io.pebbletemplates</groupId>
            <artifactId>pebble-spring-boot-starter</artifactId>
            <version>${pebble.version}</version>
        </dependency>

        <!-- JDBC驱动 -->
        <dependency>
            <groupId>org.hsqldb</groupId>
            <artifactId>hsqldb</artifactId>
        </dependency>
    </dependencies>
</project>
```

使用Spring Boot时，强烈推荐从`spring-boot-starter-parent`继承，因为这样可以引入Spring Boot的预置配置

紧接着我们引入可依赖`spring-boot-starter-web`和`spring-boot-starter-jdbc`，他们分别引入了SpringMVC相关依赖和SpringJDBC相关依赖，无需指定版本号，因为引入的`<parent>`内已经制定了，只有我们自己引入的某些第三方jar包需要指定版本号，这里我们引入`pebble-spring-boot-starter`当作View，以及`hsqldb`作为嵌入式数据库，`hsqldb`已经在`spring-boot-starter-jdbc`预置了版本号，因此无需指定

Spring Boot自动装配功能是通过自动扫描+条件装配实现的，这一套机制在默认情况下工作得很好，但是，如果我们要手动控制某个Bean的创建，就需要详细地了解Spring Boot自动创建的原理，很多时候还要跟踪`XxxAutoConfiguration`，以便设定条件使得某个Bean不会被自动创建。

## 二. 使用开发者工具

Spring Boot提供了一个开发者工具，可以监控classpath路径上的文件。只要源码或配置文件发生修改，Spring Boot应用可以自动重启。只需添加如下依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>
```

## 三. 打包Spring Boot应用

在SpringBoot应用中打包比较简单

```xml
<project ...>
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

执行如下命令即可打包

```shell
mvn clean package
```

以`commandcenter`项目为例，打包后可以在`target`目录下看到一个jar文件`commandcenter-1.0-SNAPSHOT`是Spring Boot打包插件创建的包含依赖的jar，可以直接运行

```
java -jar commandcenter-1.0-SNAPSHOT
```

所以，部署一个Spring Boot应用就特别简单，无需预装任何服务器，只需要上传jar包即可

## 四. 使用Actuator

在生产环境中，需要对应用程序的状态进行监控，Spring Boot中内置了一个监控功能——Actuator

在`pom.xml`中添加如下依赖即可

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

然后正常启动应用程序，Actuator会把它收集到的所有信息都暴露给JMX。此外Actuator还可以通过URL`/actuator/`挂载一些监控点，例如输入`http://localhost:8080/actuator/health`，我们可以查看应用程序当前状态：

```json
{
    "status": "UP"
}
```

许多网关作为反向代理需要一个URL来探测后端集群应用是否存活，这个URL就可以提供给网关使用

Actuator默认把所有访问点暴露给JMX，但处于安全原因，只有`health`和`info`会暴露给Web。Actuator提供的所有访问点均在官方文档列出，要暴露更多的访问点给Web，需要在`application.yml`中加上配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: info, health, beans, env, metrics
```

要特别注意暴露的URL的安全性，例如，`/actuator/env`可以获取当前机器的所有环境变量，不可暴露给公网。

## 五. 使用Profiles

Profile本身是Spring提供的功能，Profile表示一个环境的概念，如开发、测试和生产这三个环境

+   native
+   test
+   production

在启动一个Spring应用程序的时候，可以传入一个或多个环境例如

```
-Dspring.profiles.active=test,production
```

```yaml
spring:
  application:
    name: ${APP_NAME:unnamed}
  datasource:
    url: jdbc:hsqldb:file:testdb
    username: sa
    password:
    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
    hikari:
      auto-commit: false
      connection-timeout: 3000
      validation-timeout: 3000
      max-lifetime: 60000
      maximum-pool-size: 20
      minimum-idle: 1

pebble:
  suffix:
  cache: false

server:
  port: ${APP_PORT:8080}

---

spring:
  profiles: test

server:
  port: 8000

---

spring:
  profiles: production

server:
  port: 80

pebble:
  cache: true
```

---最前面的配置是默认配置，不需要指定profile，后面的每段配置都必须以`spring.profiles.xxx`开头，表示一个Profile。上述配置默认使用8080端口，但是在test环境下使用8000端口，在production环境下使用80端口，并启用Pebble的缓存

可以使用`@Profile(default)`和`@Profile(!default)`来决定真正装载哪一个

## 六. 使用Conditional

Spring Boot为我们准备了几个非常有用的条件：

-   @ConditionalOnProperty：如果有指定的配置，条件生效；
-   @ConditionalOnBean：如果有指定的Bean，条件生效；
-   @ConditionalOnMissingBean：如果没有指定的Bean，条件生效；
-   @ConditionalOnMissingClass：如果没有指定的Class，条件生效；
-   @ConditionalOnWebApplication：在Web环境中条件生效；
-   @ConditionalOnExpression：根据表达式判断条件是否生效。

以`@ConditionalOnProperty`为例，先定义配置storage.type=xxx。用来判断条件，默认为`local`

```yaml
storage:
  type: ${STORAGE_TYPE:local}
```

设定为`local`时，启用`LocalStorageService`：

```java
@Component
@ConditionalOnProperty(value = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {
    ...
}
```

设定为`aws`时，启用`AwsStorageService`：

```java
@Component
@ConditionalOnProperty(value = "storage.type", havingValue = "aws")
public class AwsStorageService implements StorageService {
    ...
}
```

设定为`aliyun`时，启用`AliyunStorageService`：

```java
@Component
@ConditionalOnProperty(value = "storage.type", havingValue = "aliyun")
public class AliyunStorageService implements StorageService {
    ...
}
```

注意到`LocalStorageService`的注解，当指定配置为`local`，或者配置不存在，均启用`LocalStorageService`。

## 七. 加载配置文件

加载配置文件可以直接使用注解`@Value`，但是多次引用同一个`@Value`不但麻烦，而且`@Value`使用字符串，缺少编译器检查，容易造成多处引用不一致，为了更好的管理配置，Spring Boot允许创建一个Bean，持有一组配置，并由Spring Boot自动注入

```yaml
storage:
  local:
    # 文件存储根目录:
    root-dir: ${STORAGE_LOCAL_ROOT:/var/storage}
    # 最大文件大小，默认100K:
    max-size: ${STORAGE_LOCAL_MAX_SIZE:102400}
    # 是否允许空文件:
    allow-empty: false
    # 允许的文件类型:
    allow-types: jpg, png, gif
```

可以首先定义一个Java Bean，持有该组配置：

```java
//保证Java Bean的属性名称与配置一致极了，然后添加两个注解
@Configuration
@ConfigurationProperties("storage.local")
public class StorageConfiguration {

    private String rootDir;
    private int maxSize;
    private boolean allowEmpty;
    private List<String> allowTypes;

    // TODO: getters and setters
}
```

`@ConfigurationProperties("storage.local")`表示将从配置项`storage.local`读取该项的所有子项配置，并且`@Configuration`表示`StorageConfiguration`也是一个Spring管理的Bean，可直接注入到其他的Bean中

这样就只需要注入`StorageConfiguration`这个Bean，然后由编译器检查类型，无需编写重复的`@Value`注解

## 八. 禁用自动配置

Spring Boot会自动创建出`DataSource`、`JdbcTemplate`、`DataSourceTransactionManager`，非常方便。但是，有时候，我们又必须要禁用某些自动配置。例如，系统有主从两个数据库，而Spring Boot的自动配置只能配一个，怎么办？

这个时候，针对`DataSource`相关的自动配置，就必须关掉。我们需要用`exclude`指定需要关掉的自动配置：

```java
@SpringBootApplication
// 启动自动配置，但排除指定的自动配置:
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class Application {
    ...
}
```

现在，Spring Boot不再给我们自动创建`DataSource`、`JdbcTemplate`和`DataSourceTransactionManager`了，要实现主从数据库支持，怎么办？

让我们一步一步开始编写支持主从数据库的功能。首先，我们需要把主从数据库配置写到`application.yml`中，仍然按照Spring Boot默认的格式写，但`datasource`改为`datasource-master`和`datasource-slave`：

```java
spring:
  datasource-master:
    url: jdbc:hsqldb:file:testdb
    username: sa
    password:
    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
  datasource-slave:
    url: jdbc:hsqldb:file:testdb
    username: sa
    password:
    dirver-class-name: org.hsqldb.jdbc.JDBCDriver
```

注意到两个数据库实际上是同一个库。如果使用MySQL，可以创建一个只读用户，作为`datasource-slave`的用户来模拟一个从库。

下一步，我们分别创建两个HikariCP的`DataSource`：

```java
public class MasterDataSourceConfiguration {
    @Bean("masterDataSourceProperties")
    @ConfigurationProperties("spring.datasource-master")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("masterDataSource")
    DataSource dataSource(@Autowired @Qualifier("masterDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }
}

public class SlaveDataSourceConfiguration {
    @Bean("slaveDataSourceProperties")
    @ConfigurationProperties("spring.datasource-slave")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("slaveDataSource")
    DataSource dataSource(@Autowired @Qualifier("slaveDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }
}
```

注意到上述class并未添加`@Configuration`和`@Component`，要使之生效，可以使用`@Import`导入：

```java
@SpringBootApplication
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import({ MasterDataSourceConfiguration.class, SlaveDataSourceConfiguration.class})
public class Application {
    ...
}
```

此外，上述两个`DataSource`的Bean名称分别为`masterDataSource`和`slaveDataSource`，我们还需要一个最终的`@Primary`标注的`DataSource`，它采用Spring提供的`AbstractRoutingDataSource`，代码实现如下：

```java
class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // 从ThreadLocal中取出key:
        return RoutingDataSourceContext.getDataSourceRoutingKey();
    }
}
```

`RoutingDataSource`本身并不是真正的`DataSource`，它通过Map关联一组`DataSource`，下面的代码创建了包含两个`DataSource`的`RoutingDataSource`，关联的key分别为`masterDataSource`和`slaveDataSource`：

```java
public class RoutingDataSourceConfiguration {
    @Primary
    @Bean
    DataSource dataSource(
            @Autowired @Qualifier("masterDataSource") DataSource masterDataSource,
            @Autowired @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        var ds = new RoutingDataSource();
        // 关联两个DataSource:
        ds.setTargetDataSources(Map.of(
                "masterDataSource", masterDataSource,
                "slaveDataSource", slaveDataSource));
        // 默认使用masterDataSource:
        ds.setDefaultTargetDataSource(masterDataSource);
        return ds;
    }

    @Bean
    JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    DataSourceTransactionManager dataSourceTransactionManager(@Autowired DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
```