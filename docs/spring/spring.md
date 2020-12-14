# Spring

[TOC]

## 一. IoC容器

什么是容器？容器是一种为特定组件的运行提供必要支持的一个软件环境。比如docker和tomcat。使用容器除了提供运行环境，还提供了许多底层服务。spring的核心就是提供了一个IOC容器，它可以管理所有轻量级的JavaBean组件，提供的底层服务包括组建的生命周期管理、配置和组装服务、AOP支持。以及建立在AOP基础上的声明式事务服务等。

在第一章中介绍的IoC容器，主要介绍spring容器如何对组件进行生命周期管理和配置组装服务。

## 1. IoC原理

传统开发方式的缺点

+ 实例化一个组件比较困难
+ 没有必要让每个服务都创建同一个实例，可以共享，但是谁负责创建谁负责获取他人创建的是个问题
+ 很多组建使用完毕需要销毁来释放资源，如果该组件被多个组件共享，如何确保全部被销毁
+ 随着组件的越多，组件的依赖关系会更加复杂
+ 测试某个组件是复杂的，因为必须要在真实的数据库环境下执行

传统的应用程序中，控制权在程序本身，程序的控制流程完全由开发者控制，这种模式的缺点是，一个组件如果要使用另一个组件，必须先知道如何正确地创建它。

在IoC模式下，控制权发生了反转，即从应用程序转移到了IoC容器，所有组件不在由应用程序自己创建和配置，而是由IoC容器负责，这样，应用程序只需要直接使用已经创建好并且配置好的组件。为了能让组件在IoC容器中被装配出来，需要某种注入机制。

```java
public class BookService{
  private DataSource dataSource;
  //依赖注入可以通过set()方法实现，但是也可以通过下面的构造方法来实现
  public void setDataSource(DataSource dataSource){
    this.dataSource=dataSource;
  }
  
  public BookService(DataSource dataSource){
    this.dataSource=dataSource;
  }
}
```

好处有以下三点

+ `BookService`不再关心如何创建`DataSource`，因此也不需编写读取数据库配置之类的代码。
+ `DataSource`实现了共享
+ 测试`BookService`更容易，因为注入的是`DataSource`，可以使用内存数据库，而不是真实的SQL配置

因此IoC又称之为依赖注入，它解决了最主要的一个问题，组件的创建+配置与组件的使用分离，并且由IoC负责管理组件的生命周期

那容器如何创建组件，以及各组件的依赖关系，可以使用两种方式分别是XML配置文件和注解

在spring的IoC容器中，我们把所有组件统称为JavaBean，即配置一个组件就是配置一个Bean

## 2. 装配Bean

编写`application.xml`文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="userService" class="com.itranswarp.learnjava.service.UserService">
        <property name="mailService" ref="mailService" />
        <property name="password" value="password" />
    </bean>

    <bean id="mailService" class="com.itranswarp.learnjava.service.MailService" />
  
</beans>
```

上面的头部分是一样的，只需要关注bean的配置

+ 每个`<bean ...>`都有一个id标识，相当于Bean的唯一ID
+ 在`userService`Bean中，通过`property name="..." ref="..."/>`注入了另一个Bean
+ Bean的顺序不重要，spring会根据依赖关系自动正确初始化

上述的XML配置文件用Java代码写出来就像这样

```java
UserService userService=new UserService();
MailService mailService=new MailService();
userService.setMailService(mailService);
```

spring容器是通过读取XML文件后使用反射完成的

如果注入的不是Bean而是boolean、int、String这样的数据类型，则通过value注入

```java
public class Main{
  public static void main(String[] args){
    //创建一个Spring的Ioc容器实例，然后加载配置文件，让Spring容器为我们创建并装配好配置文件中指定的所有Bean
    ApplicationContext context=new ClassPathXmlApplicationContext("application.xml");
    //从ApplicationContext中根据Bean的ID获取Bean，但更多的时候我们根据Bean的类型获取Bean的引用
    UserService userService=context.getBean(UserService.class);
    User user=userService.login("a@example.com","password");
    System.out.println(user.getName());
  }
}
```

小结

+ spring的IoC接口是`ApplicationContext`，并提供了多种实现类；
+ 通过XML配置文件创建IoC容器时，使用`ClassPathXmlApplicationContext`;
+ 实例化IoC容器后，通过`getBean()`方法获取Bean的引用

## 3. 使用Annotation配置

XML文件过于繁琐，每增加一个组件，就必须把新的Bean配置到XML中，所以使用注解的方式

```java
@Component
public class UserService {
  @Autowired
  MailService mailService;
  ...;
  MailService mailService;
  public UserService(@Autowired MailService mailService){
    this.mailService=mailService;
  }
}
```

`@Component`注解代表定义了一个Bean，它有一个可选的名称，默认是userService，即小写开头的类名。

`@Autowired`注解相当于把指定类型的Bean注入到指定的字段中。和XML相比，它大幅度简化了注入，因为它不但可以写在`set()`方法上，还可以直接写在字段上，甚至可以写在构造方法中

```java
@Configuration
@ComponentScan
public class AppConfig {
  public static void main(String[] args) {
    ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
    UserService userService = context.getBean(UserService.class);
    User user = userService.login("bob@example.com", "password");
    System.out.println(user.getName());
  }
}
```

`@Configuration`注解代表它是一个配置类，因为使用了`AnnotationConfigApplicationContext`，必须传入了一个标注为`@Configuration`的类名

`@ComponentScan`注解告诉容器，自动搜索当前类所在的包和子包，把所有标注为`@Component`的Bean自动创建出来，并根据`@Autowired`进行装配

使用Annotation注意事项

+ 每个Bean被标注为`@Component`并正确使用`@Autowired`注入
+ 配置类被标注为`@Configuration`和`ComponentScan`
+ 所有Bean均在指定包以及子包内，启动类放置到顶层包中

## 4. 定制Bean

+ Scope

对于spring容器来说，但我们把一个Bean标记为`@Component`后，它就自动为我们创建一个单例，即容器初始化时创建Bean，容器关闭前销毁Bean。在容器运行期间，我们调用`getBean(class)`获取到的Bean总是同一个实例

还有一种Bean，我们在每次调用`getBean(class)`容器都反悔一个新的实例，这种Bean成为`Prototype`，它的生命周期显然与丹利不同。生命一个`prototype`的Bean时，需要添加一个额外的`@Scope`注解

```java
@Component
@Scope("prototype")
public class MailService{
  ...;
}
```

+ 注入List

有时候，会有一些借口相同但是不同实现类的Bean。例如注册用户时，我们要对email、password和name这三个变量进行验证，先定义验证借口

```java
public interface Validator {
  void validate(String email, String password, String name);
}
```

使用三个validator对用户参数进行验证

```java
@Component
@Order(1)//指定Bean的顺序
public class EmailValidator implements Validator {
  public void validate(String email, String password, String name) {
    if (!email.matches("^[a-z0-9]+\\@[a-z0-9]+\\.[a-z]{2,10}$")) {
      throw new IllegalArgumentException("invalid email: " + email);
    }
  }
}

@Component
@Order(2)
public class PasswordValidator implements Validator {
  public void validate(String email, String password, String name) {
    if (!password.matches("^.{6,20}$")) {
      throw new IllegalArgumentException("invalid password");
    }
  }
}

@Component
@Order(3)
public class NameValidator implements Validator {
  public void validate(String email, String password, String name) {
    if (name == null || name.isBlank() || name.length() > 20) {
      throw new IllegalArgumentException("invalid name: " + name);
    }
  }
}
```

通过一个Validators作为入口进行验证

```java
@Component
public class Validators{
  //Validators被注入了一个List<Validator>,spring会自动把所有类型为Validator的Bean装配为一个List注入进来，这样每增加一个Validator类型，就自动被Spring装配到Validators中。
  @Autowired
  List<Validator> validators;
  
  public void validate(String mail,String password,String name){
    for(var validator:this.validators){
      validator.validate(email,password,name);
    }
  }
}
```

+ 可选注入

默认情况下，当我们标记了一个`@Autowired`后，spring如果没有找到对应类型的Bean，就会抛出`NoSuchBeanDefinitionException`异常，可以给`@Autowired`增加一个`required=false`，这个参数会告诉Spring容器，如果找到一个ZoneId的Bean就注入，如果没找到就忽略。

```java
@Component
public class MailService {
    @Autowired(required = false)
    ZoneId zoneId = ZoneId.systemDefault();
    ...
}
```

这种方式特别适合有定义就是用定义的，没有就是用默认值的情况

+ 创建第三方Bean

如果一个Bean没有在我们自己的package管理之内，例如ZoneId，我们需要在`@Configuration`配置类中编写一个java方法创建并返回它，并且要给方法加上一个`@Bean`标记

```java
@Configuration
@ComponentScan
public class AppConfig{
  @Bean   //Spring对标记为Bean的方法只调用一次。因此返回的Bean仍然是单例
  ZoneId createZoneId(){
    return ZoneId.of("z");
  }
}
```

+ 初始化和销毁

在Bean的初始化和清理方法上标记`@PostConstruct`和`@PreDestroy`

```java
@Component
public class MailService {
  @Autowired(required = false)
  ZoneId zoneId = ZoneId.systemDefault();

  @PostConstruct
  public void init() {
    System.out.println("Init mail service with zoneId = " + this.zoneId);
  }

  @PreDestroy
  public void shutdown() {
    System.out.println("Shutdown mail service");
  }
}
```

Spring容器会对上述Bean作如下初始化流程：

​1）调用构造方法创建MailService实例

​2）根据`@Autowired`进行注入

​3）使用标记有`@PostConstruct`的`init()`方法进行初始化

而销毁时，容器会先吊用标记有`@PreDestory`的`shutdown()`方法，spring只根据Annotation查找无参方法，对方法名不作要求。

+ 使用别名

默认情况对一种类型的Bean，容器只创建一个实例。但有时，需要对同一种类型的Bean创建多个实例。同时连接多个数据库，就必须创建多个DataSource实例，如果在`@Configuration`类中创建多个同类型Bean,spring抛出`NoUniqueBeanDefinitionException`，这个时候需要别名

```java
@Configuration
@ComponentScan
public class AppConfig {
  @Bean("z")
  @Primary //将这个Bean指定为主要Bean，当注入时没有指定名字，就会注入标记@Primary的Bean，比如主从两个数据源
  ZoneId createZoneOfZ() {
    return ZoneId.of("Z");
  }

  @Bean
  @Qualifier("utc8")
  ZoneId createZoneOfUTC8() {
    return ZoneId.of("UTC+08:00");
  }
}
```

可以使用`@Bean("name")`指定别名，也可以用`@Bean+@Qulifier("name")`指定别名，存在多个同类型的Bean时，注入ZoneId会报错

```java
NoUniqueBeanDefinitionException: No qualifying bean of type 'java.time.ZoneId' available: expected single matching bean but found 2
```

因此在注入时也要指定Bean的名称

```java
@Component
public class MailService {
  @Autowired(required = false)
  @Qualifier("z") // 指定注入名称为"z"的ZoneId
  ZoneId zoneId = ZoneId.systemDefault();
    ...
}
```

+ 使用FactoryBean

Spring提供了工厂模式，允许定义一个工厂，然后由工厂创建真正的Bean，用工厂模式创建Bean需要实现`FactoryBean`接口

```java
@Component
public class ZoneIdFactoryBean implements FactoryBean<ZoneId>{
  String zone="z";
  @Override
  public ZoneId getObject() throw Exception{
    return ZoneId.of(zone);
  }
  @Override
  public Class<?> getObjectType(){
    return ZoneId.class;
  }
}
```

一个Bean实现了`FactoryBean`这个接口，Spring会先实例化这个工厂，然后调用`getObject()`创建真正的`Bean`。`getObjectType()`可以指定创建的Bean的类型，因为指定类型不一定与实际类型一致，可以是接口或是抽象类

因此如果定义了一个`FactoryBean`，要注意Spring创建的Bean实际上是这个`FactoryBean`的`getObject()`方法返回的Bean，为了和普通Bean区分，通常以`XxxFactoryBean`命名

## 5. 使用Resource

在Java中我们经常要读区配置文件、资源文件等。使用Spring容器时，我们也可以把文件注入进来，方便程序读取。

在Spring中提供了一个`org.springframework.core.io.Resource`(注意不是`javax.annotation.Resource`)，它可以想String、int一样使用`@Value`注入

```java
@Component
public class AppService {
    @Value("classpath:/logo.txt")//@Value("file/path/to/logo.txt")可以指定文件路径
    private Resource resource;

    private String logo;

    @PostConstruct
    public void init() throws IOException {
        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            this.logo = reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
```

maven项目中，资源文件和配置文件直接放到Resource目录下即可

## 6. 注入配置

常用的额配置方法一般是以`key=value`的形式写在`.properties`文件中，虽然可以用到第五节中讲的Resource来读取配置文件，但是也过于繁琐，Spring容器还提供了一个更简单的`@PropertyResource`来自动读取配置文件，需要在`@Configuration`配置类上加注解

```java
@Configuration
@ComponentScan
@PropertySource("app.properties") // 表示读取classpath的app.properties，Spring看到这个注解，会自动读取配置文件
public class AppConfig {
  //@Value("${app.zone}") 表示读取key为app.zone的value，如果key不存在，启动会报错
  @Value("${app.zone:Z}") //然后使用@Value自动注入，表示读取key为app.zone的value，如果key不存在，是用默认值Z
  String zoneId;

  @Bean
  ZoneId createZoneId() {
    return ZoneId.of(zoneId);
  }
}
```

还可以把注解写到方法参数中

```java
@Bean
ZoneId createZoneId(@Value("${app.zone:Z}") String zoneId) {
  return ZoneId.of(zoneId);
}
```

另一种注入的方式，实现通过一个JavaBean持有所有的配置，例如一个SmtpConfig

```java
@Component
public class SmtpConfig {
  @Value("${smtp.host}")
  private String host;

  @Value("${smtp.port:25}")
  private int port;

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
```

然后在需要读取的地方使用`#{smtpConfig.host}`注入

注意`#{}`这种注入语法，他和`${key}`不同的是，`#{}`表示从JavaBean中读取属性，`#{smtpConfig.host}`的意思是，从名称为stmpConfig的Bean读取host属性，即调用`getHost()`方法。一个Class名为SmtpConfig的Bean，它在Spring容器中的默认名称就是`smtpConfig`，除非用`@Qulifier`指定了名称

使用一个独立的JavaBean持有所有属性，然后在其他Bean中以`#{bean.property}`注入的好处是，多个Bean都可以引用同一个Bean的某一个属性。例如，如果SmtpConfig决定从数据库中读取相关配置项，那么MailService注入的`@Value("#{smtpConfig.host}")`仍然可以不修改正常运行

小结：

+ Spring容器可以通过`@PropertySource`自动读取配置，并以`@Value("${key}")`的形式注入
+ 可以通过`@Value("${key:defaultValue}")指定默认值`
+ 以`@Value("#{bean.property}")`注入时，Spring容器自动把指定Bean的指定属性值注入

## 7. 使用条件装配

Spring允许通过`Profile`配置不同的Bean

Spring还提供了`@Conditional`来进行条件装配，SpringBoot在此基础上进一步提供了机遇配置、Class、Bean等条件进行装配

+++

## 二. 使用AOP

每个业务方法，除了自身的逻辑以外还需要安全检查、日志记录和事务处理，而对于安全、日志、事务等代码，会出现在每个业务方法中。使用OOP很难讲这些四处分散的代码模块化。一种可行的方法是使用Proxy模式，将某个功能如权限检查放入到Proxy中，这种方式的缺点是比较麻烦，必须先抽取接口，然后针对每个方法实现Proxy，另一种方法则是切面

+ AOP原理

在Java平台上，对于AOP的织入有三种方式

+ 编译期：在编译时，由编译器把切面调用编译进字节码，这种方式需要定义新的关键字并扩展编译器，AspectJ就扩展了Java编译器，使用aspect关键字来实现织入
+ 类加载器：在目标类被加载到JVM时，通过一个特殊的类加载器，对目标类的字节码重新增强
+ 运行期：目标对象和切面都是普通Java类，通过JVM的动态代理功能或者第三方库实现运行期动态织入

Spring的AOP实现就是第三种，由于JVM的动态代理要求必须实现接口，如果一个类没有业务接口，就需要通过CGLIB或者Javassist这些第三方库实现

## 1.装配AOP

AOP中的一些概念

+ Aspect：切面，即一个横跨多个核心逻辑的功能，或者称之为系统关注点
+ Joinpoint：连接点，即定义在应用程序流程的何处插入切面的执行
+ Pointcut：切入点，即一组连接点的集合
+ Advice：增强，指特定连接点上执行的动作
+ Introduction：引介，指为一个已有的Java对象动态地增加新的接口
+ Weaving：织入，指将切面整合到程序的执行流程中
+ Interceptor：拦截器，一种实现赠钱的方式
+ Target Object：目标对象。即真正执行业务的核心逻辑对象
+ AOP Proxy：AOP代理，是客户端持有的增强后的对象引用

```java
@Aspect //表示他标注的@Before方法和@Around方法需要在对应的方法前面或者前后执行
@Component
public class LoggingAspect {
  // 在执行UserService的每个方法前执行:
  @Before("execution(public * com.itranswarp.learnjava.service.UserService.*(..))")
  public void doAccessCheck() {
    System.err.println("[Before] do access check...");
  }

  // 在执行MailService的每个方法前后执行:
  @Around("execution(public * com.itranswarp.learnjava.service.MailService.*(..))")
  public Object doLogging(ProceedingJoinPoint pjp) throws Throwable {
    System.err.println("[Around] start " + pjp.getSignature());
    Object retVal = pjp.proceed();
    System.err.println("[Around] done " + pjp.getSignature());
    return retVal;
  }
}
```

```java
@Configuration
@ComponentScan
//Spring的IoC容器看到这个注解，就会自动查找带有@Aspect的Bean，然后根据每个方法的@Before@Around灯珠姐把AOP注入到特定的Bean中
@EnableAspectJAutoProxy
public class AppConfig {
  ...
}
```

那这是如何实现的，即如何将LogginAspect定义的方法注入到其他Bean的呢

以`LogginAspect.doAccessCheck()`为例，要把它注入到`UserService`的每个`public`方法中，最简单的实现方法就是编写一个子类，并持有原始实例的引用。

```java
public UserServiceAopProxy extends UserService {
  private UserService target;
  private LoggingAspect aspect;

  public UserServiceAopProxy(UserService target, LoggingAspect aspect) {
    this.target = target;
    this.aspect = aspect;
  }

  public User login(String email, String password) {
    // 先执行Aspect的代码:
    aspect.doAccessCheck();
    // 再执行UserService的逻辑:
    return target.login(email, password);
  }

  public User register(String email, String password, String name) {
    aspect.doAccessCheck();
    return target.register(email, password, name);
  }

  ...
}
```

这些都是Spring容器启动时为我们自动创建的注入了Aspect的子类，它取代了原始的UserService（原始的UserService实例作为内部变量隐藏在UserServiceAopProxy中）。如果打印从Spring容器获取的UserService实例类型，它类似`UserService$$EnhancerBySpringCGLIB$$1f44e01c`，实际上是Spring使用CGLIB动态创建的子类，但对于调用方来说，感觉不到任何区别

Spring**对接口类型使用JDK动态代理，对普通类使用CGLIB创建子类，如果一个BEan的class是final，Spring将无法为其创建子类**

使用AOP一共分三步

+ 定义执行方法，并在方法上通过AspectJ的注解告诉Spring应该在何处调用此方法

+ 标记`@Component`和`@Aspect`
+ `在@Configuration`类上标注`@EnableAspectJAutoProxy`

拦截器类型

+ `@Before`：这种拦截器先执行拦截器代码，再执行目标代码，如果拦截器抛异常，那么目标代码就不执行了
+ `@After`：这种拦截器先执行目标代码，再执行拦截器代码，无论目标代码是否抛异常，拦截器代码都执行
+ `@AfterReturning`：和`@After`不同的是，只有当目标代码正常返回时，才执行拦截器代码
+ `@AfterThrowing`：和`@After`不同的是，只有当目标代码抛出异常时，才执行拦截器代码
+ `@Around`：能够完全控制目标代码是否执行，并可以在执行前后、抛异常前后执行任意拦截代码，包含上面所有功能

## 2. 使用注解装配AOP

自定义一个性能监控的注解

```java
@Target(METHOD)
@Retention(RUNTIME)
public @interface MetricTime{
  String value();
}
```

在需要被监控的关键方法上标注该注解

```java
@Component
public class UserService{
  @MetricTime("register")
  public User rigister(String eamil,String password,String name){
    ...
  }
}
```

然后定义MetricAspect

```java
@Aspect
@Component
public class MetricAspect{
  @Around("@annotation(metricTime)")
  public Object metric(ProceedingJoinPoint joinPoint,MetricTime metricTime){
    String name = metricTime.value();
    long start=System.currentTimeMillis();
    try{
      return joinPoint.proceed();
    }finally {
      long t=System.currentTimeMillis()-start;
      // 写入日志或发送至JMX:
      System.err.println("[Metrics] " + name + ": " + t + "ms");
    }
  }
}
```

注意`metric()`方法标注了`@Around("@annotation(metricTime)")`，它的意思是，符合条件的目标方法是带有`@MetricTime`注解的方法，因为`metric()`方法参数类型是`MetricTime`（注意参数名是`metricTime`不是`MetricTime`），我们通过它获取性能监控的名称

有了`@MetricTime`注解，再配合`MetricAspect`，任何Bean，只要方法标注了`@MetricTime`注解，就可以自动实现性能监控。输出结果如下

```java
Welcome, Bob!
[Metrics] register: 16ms
```

## 3. AOP注意事项

对于Sprin通过CGLIB动态创建的`UserService$$EnhancerBySpringCGLIB`代理类，它的构造方法中，并未调用`super()`，因此，从父类继承的成员变量，包括`final`类型的成员变量，统统都没有初始化

问题：Java语言规定，任何类的构造方法，第一行必须调用`super()`，如果没有，编译器会自动加上，怎么Spring的CGLIB就可以搞特殊？

这是因为自动加`super()`的功能是Java编译器实现的，它发现你没加，就自动给加上，发现你加错了，就报编译错误。但实际上，如果直接构造字节码，一个类的构造方法中，不一定非要调用`super()`。Spring使用CGLIB构造的Proxy类，是直接生成字节码，并没有源码-编译-字节码这个步骤因此

Spring**通过CGLIB创建的代理类，不会初始化代理类自身集成的任何成员变量，包括final类型的成员变量**

修复也很简单，可以将直接访问字段的代码，改为通过方法访问

**问题1.为什么Spring可以不初始化Proxy集成的字段？？？**

```java
@Component
public class MailService {
  @Value("${smtp.from:xxx}")
  String mailFrom;

  SmtpSender sender;

  @PostConstruct
  public void init() {
    //sender初始化需要依赖其他注入，并且已经初始化了一次，proxy类没法正确初始化sender
    sender = new SmtpSender(mailFrom, ...);
  }

  public void sentMail(String to) {
    ...
  }
}
```

+ 因为你初始化的时候可能用到注入的其他类，主要原因就是Spring无法在逻辑上正常初始化proxy的字段，所以干脆不初始化，并通过NPE直接暴露出来
+ 还有一个原因是如果对字段进行修改，proxy的字段其实根本没改

```java
@Component
public class MailService {
  String status = "init";

  public void sentMail(String to) {
    this.status = "sent";
  }
}
```

因为只有原始Bean的方法会对自己的字段进行修改，它无法改proxy的字段

**问题2.如果一个Bean不允许任何AOP代理，应该怎么做来保护自己在运行期不会被代理？？？**

将类设置为final防止CGLIB创建Proxy，并且不继承接口防止JDK自带的动态代理

## 三. 访问数据库

Spring为了简化数据库访问，做了以下几点：

+ 提供了简化的访问JDBC的模板类，不比手动释放资源
+ 提供了一个统一的DAO类以实现Data Access Objcet资源
+ 把`SQLException`封装为`DataAccessException`，这个异常是一个`RuntimeException`，并且让我们能区分SQL异常的原因。例如`DuplicateException`表示违反了一个唯一约束
+ 能方便即成Hibernate、JPA和Mybatis这些数据库访问框架

## 1. 使用JDBC

Spring提供的`JdbcTemplate`采用Template模式，提供了一系列以回调为特点的工具方法，目的是避免繁琐的`try...catch`语句。

```java
//T execute(ConnectionCallback<T> action)的用法
public User getUserById(long id) {
  // 注意传入的是ConnectionCallback:
  return jdbcTemplate.execute((Connection conn) -> {
    // 可以直接使用conn实例，不要释放它，回调结束后JdbcTemplate自动释放:
    // 在内部手动创建的PreparedStatement、ResultSet必须用try(...)释放:
    try (var ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
      ps.setObject(1, id);
      try (var rs = ps.executeQuery()) {
        if (rs.next()) {
          return new User( // new User object:
            rs.getLong("id"), // id
            rs.getString("email"), // email
            rs.getString("password"), // password
            rs.getString("name")); // name
        }
        throw new RuntimeException("user not found by id.");
      }
    }
  });
}
```

即上面的方法允许获取Connection然后做任何基于Connection的操作,任何JDBC操作都可以使用这个方法。

```java
//T execute(String sql, PreparedStatementCallback<T> action)的用法：
public User getUserByName(String name) {
  // 需要传入SQL语句，以及PreparedStatementCallback:
  return jdbcTemplate.execute("SELECT * FROM users WHERE name = ?", (PreparedStatement ps) -> {
    // PreparedStatement实例已经由JdbcTemplate创建，并在回调后自动释放:
    ps.setObject(1, name);
    try (var rs = ps.executeQuery()) {
      if (rs.next()) {
        return new User( // new User object:
          rs.getLong("id"), // id
          rs.getString("email"), // email
          rs.getString("password"), // password
          rs.getString("name")); // name
      }
      throw new RuntimeException("user not found by id.");
    }
  });
}
```

```java
//T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper)方法：
public User getUserByEmail(String email) {
  // 传入SQL，参数和RowMapper实例:
  return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?", new Object[] { email },
                                     (ResultSet rs, int rowNum) -> {
                                       // 将ResultSet的当前行映射为一个JavaBean:
                                       return new User( // new User object:
                                         rs.getLong("id"), // id
                                         rs.getString("email"), // email
                                         rs.getString("password"), // password
                                         rs.getString("name")); // name
                                     });
}
```

在`queryForObject()`方法中，传入SQL以及SQL参数后，`JdbcTemplate`会自动创建`PreparedStatement`，自动执行查询并返回`ResultSet`，我们提供的`RowMapper`需要做的事情就是把`ResultSet`的当前行映射成一个JavaBean并返回。整个过程中，使用`Connection`、`PreparedStatement`和`ResultSet`都不需要我们手动管理。

`RowMapper`不一定返回JavaBean，实际上它可以返回任何Java对象。例如，使用`SELECT COUNT(*)`查询时，可以返回`Long`：

```java
public long getUsers() {
  return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", null, (ResultSet rs, int rowNum) -> {
    // SELECT COUNT(*)查询只有一列，取第一列数据:
    return rs.getLong(1);
  });
}
```

如果我们期望返回多行记录，而不是一行，可以用`query()`方法：

```java
public List<User> getUsers(int pageIndex) {
  int limit = 100;
  int offset = limit * (pageIndex - 1);
  return jdbcTemplate.query("SELECT * FROM users LIMIT ? OFFSET ?", new Object[] { limit, offset },
                            new BeanPropertyRowMapper<>(User.class));
}
```

上述`query()`方法传入的参数仍然是SQL、SQL参数以及`RowMapper`实例。这里我们直接使用Spring提供的`BeanPropertyRowMapper`。如果数据库表的结构恰好和JavaBean的属性名称一致，那么`BeanPropertyRowMapper`就可以直接把一行记录按列名转换为JavaBean。

如果我们执行的不是查询，而是插入、更新和删除操作，那么需要使用`update()`方法：

```java
public void updateUser(User user) {
  // 传入SQL，SQL参数，返回更新的行数:
  if (1 != jdbcTemplate.update("UPDATE user SET name = ? WHERE id=?", user.getName(), user.getId())) {
    throw new RuntimeException("User not found by id");
  }
}
```

只有一种`INSERT`操作比较特殊，那就是如果某一列是自增列（例如自增主键），通常，我们需要获取插入后的自增值。`JdbcTemplate`提供了一个`KeyHolder`来简化这一操作：

```java
public User register(String email, String password, String name) {
  // 创建一个KeyHolder:
  KeyHolder holder = new GeneratedKeyHolder();
  if (1 != jdbcTemplate.update(
    // 参数1:PreparedStatementCreator
    (conn) -> {
      // 创建PreparedStatement时，必须指定RETURN_GENERATED_KEYS:
      var ps = conn.prepareStatement("INSERT INTO users(email,password,name) VALUES(?,?,?)",
                                     Statement.RETURN_GENERATED_KEYS);
      ps.setObject(1, email);
      ps.setObject(2, password);
      ps.setObject(3, name);
      return ps;
    },
    // 参数2:KeyHolder
    holder)
     ) {
    throw new RuntimeException("Insert failed.");
  }
  // 从KeyHolder中获取返回的自增值:
  return new User(holder.getKey().longValue(), email, password, name);
}
```

`JdbcTemplate`还有许多重载方法，这里我们不一一介绍。需要强调的是，`JdbcTemplate`只是对JDBC操作的一个简单封装，它的目的是尽量减少手动编写`try(resource) {...}`的代码，对于查询，主要通过`RowMapper`实现了JDBC结果集到Java对象的转换。

我们总结一下`JdbcTemplate`的用法，那就是：

+ 针对简单查询，优选`query()`和`queryForObject()`，因为只需提供SQL语句、参数和`RowMapper`；
+ 针对更新操作，优选`update()`，因为只需提供SQL语句和参数；
+ 任何复杂的操作，最终也可以通过`execute(ConnectionCallback)`实现，因为拿到`Connection`就可以做任何JDBC操作。

如果在设计表结构的时候，能够和JavaBEan的属性一一对应，那么直接使用`BeanPropertyRowMapper`就很方便，如果不一样可以使用别名

**例如表的列名是`office_address`，而JavaBean属性是`workAddress`，就需要指定别名，改写查询**

```sql
SELECT id,email,office_address AS workAddress, name FROM users WHERE email=?
```

## 2. 使用声明式事务

Spring提供了一个`PlatformTransactionManager`表示事务管理器，所有的事物都由它负责管理。而事务由`TransactionStatus`表示

Spring为了同时支持JDBC和分布式事务JTA两种事务模型，就抽象出`PlatformTransactionManager`

```java
@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource("jdbc.properties")
public class AppConfig{}
```

对需要事务支持的方法加一个`@Transaction`注解

**Spring对一个声明式事务的方法，如何开启事务支持？**原理依然是AOP代理，即通过自动创建Bean的Proxy实现

```java
public class UserService$$EnhancerBySpringCGLIB extends UserService {
  UserService target = ...
    PlatformTransactionManager txManager = ...

    public User register(String email, String password, String name) {
    TransactionStatus tx = null;
    try {
      tx = txManager.getTransaction(new DefaultTransactionDefinition());
      target.register(email, password, name);
      txManager.commit(tx);
    } catch (RuntimeException e) {
      txManager.rollback(tx);
      throw e;
    }
  }
  ...
}
```

+ 回滚事务

默认情况下，如果发生了`RuntimeException`，Spring的声明式事务将自动回滚，如果要针对`Checked Exception`回滚事务，则需要在`@Transaction`注解中写出

```java
@Transactional(rollbackFor = {RuntimeException.class, IOException.class})
public buyProducts(long productId, int num) throws IOException {
  ...
}
```

为了简化代码，强烈建议业务异常体系从`RuntimeException`中派生，这样就不必声明任何特殊异常即可让Spring的声明式事务正常工作

```java
public class BusinessException extends RuntimeException {}
public class LoginException extends BusinessException {}
public class PaymentException extends BusinessException {}
```

+ 事务边界

```java
//在使用事务的时候，明确事务边界非常重要，下面的事务边界就是register()方法的开始和结束
@Component
public class UserService {
  @Transactional
  public User register(String email, String password, String name) { // 事务开始
    ...
  } // 事务结束
}
```

```java
@Component
public class UserService {
  @Autowired
  BonusService bonusService;

  //那在这里面我们期待的事务行为是什么呢
  @Transactional
  public User register(String email, String password, String name) {
    //事务开始
    // 插入用户记录:
    User user = jdbcTemplate.insert("...");
    // 增加100积分:
    bonusService.addBonus(user.id, 100); //addBonus()也是一个事务方法
    //事务结束
  }
}
```

调用方调用`UserService.register()`这个事务方法，它在内部又调用了`bonusService.addBonus()`事务方法，一共有几个事务，如果`addBonus()`抛出了异常需要事务回滚，那`register()`方法是否也需要事务回滚呢？

对于大多数业务来说，我们期待`bonusService.addBonus()`的调用，和`UserService.register()`应当融合在一起，`UserService.register()`已经开启了一个事务，那么在内部调用`bonusService.addBonus()`时，`bonusService.addBonus()`就没必要再开启一个新事务，直接加入到`UserService.register()`的事务里就可以了

也就是只有一个事务，它的范围就是`UserService.register()`方法

+ 事务传播

`REQUIRED`：如果当前没有事务，就创建一个新事务，如果当前有事务，就加入到当前事务中执行，默认传播行为

`SUPPORTS`：表示如果有事务，就加入到当前事务，如果没有，那也不开启事务执行。这种传播级别可用于查询方法，因为SELECT语句既可以在事务内执行，也可以不需要事务；

`MANDATORY`：表示必须要存在当前事务并加入执行，否则将抛出异常。这种传播级别可用于核心更新逻辑，比如用户余额变更，它总是被其他事务方法调用，不能直接由非事务方法调用；

`REQUIRES_NEW`：表示不管当前有没有事务，都必须开启一个新的事务执行。如果当前已经有事务，那么当前事务会挂起，等新事务完成后，再恢复执行；

`NOT_SUPPORTED`：表示不支持事务，如果当前有事务，那么当前事务会挂起，等这个方法执行完成后，再恢复执行；

`NEVER`：和`NOT_SUPPORTED`相比，它不但不支持事务，而且在监测到当前有事务时，会抛出异常拒绝执行；

`NESTED`：表示如果当前有事务，则开启一个嵌套级别事务，如果当前没有事务，则开启一个新事务。

**一个事务方法是如何知道当前是否存在事务？**

使用`ThreadLocal`。spring总是把JDBC相关的`Connection`和`TransactionStatus`实例绑定到`ThreadLocal`。如果一个事务方法从`ThreadLocal`未取到事务，那么它会打开一个新的JDBC连接，同时开启一个新事务，否则，它就直接使用从`ThreadLocal`获取的JDBC连接以及`TransactionStatus`，因此事务能正确传播的前提是，方法调用是在一个线程内才行，如果像下面这样：

```java
@Transactional
public User register(String email, String password, String name) { // BEGIN TX-A
    User user = jdbcTemplate.insert("...");
    new Thread(() -> {
        // BEGIN TX-B:
        bonusService.addBonus(user.id, 100);
        // END TX-B
    }).start();
} // END TX-A
```

在另一个线程中调用`bonusService.addBonus()`他根本获取不到当前事务，因此`UserService.register()`和`bonusService.addBonus()`两个方法，将分别开启两个完全独立的事务，那如何实现跨线程传播事务，把当前线程绑定到`ThreadLocal`的`Connection`和`TransactionStatus`实例传递给新线程。

## 3. 集成MyBatis

一级缓存是指在一个Session范围内的缓存，常见的情景是根据主键查询时，两次查询可以返回同一实例

二级缓存是指跨Session的缓存，一般默认关闭需要手动配置，二级缓存极大地增加了数据的不一致性，原因在于SQL非常灵活，常常会导致意外的更新，当二级缓存生效时，两个线程读取的User实例是一样的，但是数据库对应的行记录完全可能被修改。

半自动ORM只负责把ResultSet自动映射到JavaBean，或者自动填充JavaBean参数，但仍需自己写SQL

在maven中引入依赖

```java
org.mybatis:mybatis:3.5.4
org.mybatis:mybatis-spring:2.0.4
```

```java
//创建DataSource
@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource("jdbc.properties")
public class AppConfig {
  @Bean
  DataSource createDataSource() { ... }
}
```

```java
//使用MyBatis的核心是创建SqlSessionFactory
@Bean
SqlSessionFactoryBean createSqlSessionFactoryBean(@Autowired DataSource dataSource) {
  var sqlSessionFactoryBean = new SqlSessionFactoryBean();
  sqlSessionFactoryBean.setDataSource(dataSource);
  return sqlSessionFactoryBean;
}
```

```java
//MyBatis可以直接使用Spring管理的声明式事务，因此创建事务管理器和使用JDBC是一样的
@Bean
PlatformTransactionManager createTxManager(@Autowired DataSource dataSource) {
  return new DataSourceTransactionManager(dataSource);
}
```

```java
//MyBatis使用Mapper来实现映射，而且Mapper必须是借口
public interface UserMapper {
  @Select("SELECT * FROM users WHERE id = #{id}")
  User getById(@Param("id") long id);
}
```

注意：这里的Mapper不是JdbcTemplate的RowMapper的概念，它是定义访问users表的接口方法。比如我们定义了一个`User getById(long)`的主键查询方法，不仅要定义接口方法本身，还要明确写出查询的SQL，这里用注解`@Select`标记。SQL语句的任何参数，都与方法参数按名称对应。例如，方法参数id的名字通过注解`@Param()`标记为`id`，则SQL语句里将来替换的占位符就是`#{id}`。

如果有多个参数，那么每个参数命名直接在SQL中写出对应的占位符即可

```java
@Select("SELECT * FROM users LIMIT #{offset}, #{maxResults}")
List<User> getAll(@Param("offset") int offset, @Param("maxResults") int maxResults);
```

MyBatis执行查询后，将根据方法的返回类型自动把ResultSet的每一行转换为User实例，转换规则当然是按列名和属性名对应。如果列名和属性名不同，最简单的方式是编写SELECT语句的别名

```sql
-- 列名是created_time，属性名是createdAt:
SELECT id, name, email, created_time AS createdAt FROM users
```

执行INSERT语句

```sql
@Insert("INSERT INTO users (email, password, name, createdAt) VALUES (#{user.email}, #{user.password}, #{user.name}, #{user.createdAt})")
void insert(@Param("user") User user);
```

如果users表的id是自增主键，那么我们在SQL中不传入id，但希望获取插入后的主键，需要加一个`@Options`注解

```java
@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
@Insert("INSERT INTO users (email, password, name, createdAt) VALUES (#{user.email}, #{user.password}, #{user.name}, #{user.createdAt})")
void insert(@Param("user") User user);
```

`keyProperty`和`keyColumn`分别指出JavaBean的属性和数据库的主键列名

执行update和delete方法

```java
@Update("UPDATE users SET name = #{user.name}, createdAt = #{user.createdAt} WHERE id = #{user.id}")
void update(@Param("user") User user);

@Delete("DELETE FROM users WHERE id = #{id}")
void deleteById(@Param("id") long id);
```

有了`UserMapper`接口，还需要对应的实现类才能真正执行这些数据库操作的方法。虽然可以自己写实现类，但我们除了编写`UserMapper`接口外，还有`BookMapper`、`BonusMapper`……一个一个写太麻烦，因此，MyBatis提供了一个`MapperFactoryBean`来自动创建所有Mapper的实现类。可以用一个简单的注解来启用它：

```java
@MapperScan("com.itranswarp.learnjava.mapper")
...其他注解...
public class AppConfig {
    ...
}
```

有了`@MapperScan`就可以让MyBatis自动扫描指定包的所有mapper并创建实现类。在真正的业务逻辑中，可以直接注入

```java
@Component
@Transactional
public class UserService {
    // 注入UserMapper:
    @Autowired
    UserMapper userMapper;

    public User getUserById(long id) {
        // 调用Mapper方法:
        User user = userMapper.getById(id);
        if (user == null) {
            throw new RuntimeException("User not found by id.");
        }
        return user;
    }
}
```

## 三. 开发Web应用

## 1. 使用Rest

 使用`@RestController`代替`@Controller`后，那个方法自动变成API接口方法

```java
@RestController
@RequestMapping("/api")
public class ApiController {
  @Autowired
  UserService userService;

  @GetMapping("/users")
  public List<User> users() {
    return userService.getUsers();
  }

  @GetMapping("/users/{id}")
  public User user(@PathVariable("id") long id) {
    return userService.getUserById(id);
  }

  @PostMapping("/signin")
  public Map<String, Object> signin(@RequestBody SignInRequest signinRequest) {
    try {
      User user = userService.signin(signinRequest.email, signinRequest.password);
      return Map.of("user", user);
    } catch (Exception e) {
      return Map.of("error", "SIGNIN_FAILED", "message", e.getMessage());
    }
  }

  public static class SignInRequest {
    public String email;
    public String password;
  }
}
```

如何在JSON序列化和反序列化允许写属性，禁用读属性

```java
@JsonProperty(access=Access.WRITE_ONLY)
public String getPassword(){
  return password;
}
```

## 2. 集成Filter

如果允许用户使用Basic模式进行用户验证，即在HTTP请求中添加头`Authorization: Basic email:password`，这个需求如何实现？

```java
@Component
public class AuthFilter implements Filter {
  @Autowired
  UserService userService;

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    // 获取Authorization头:
    String authHeader = req.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Basic ")) {
      // 从Header中提取email和password:
      String email = prefixFrom(authHeader);
      String password = suffixFrom(authHeader);
      // 登录:
      User user = userService.signin(email, password);
      // 放入Session:
      req.getSession().setAttribute(UserController.KEY_USER, user);
    }
    // 继续处理请求:
    chain.doFilter(request, response);
  }
}
```

在Spring中创建的这个`AuthFilter`是一个普通Bean，Servlet容器并不知道，所以它不会起作用

如果我们直接在`web.xml`中声明这个`AuthFilter`，注意`AuthFilter`的实例将由Servlet容器而不是Spring容器初始化，因此`AuthFilter`根本不生效，用于登陆的`UserService`成员变量永远是`null`

所以得通过一种方式，让Servlet容器实例化的Filter，间接引用Spring容器实例化的`AuthFilter`。SpringMVC提供了一个`DelegatingFilterProxy`

```xml
<web-app>
  <filter>
    <filter-name>authFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
  </filter>

  <filter-mapping>
    <filter-name>authFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  ...
</web-app>
```

实现原理：

+ Servlet容器从`web.xml`中读取配置，实例化`DelegatingFilterProxy`，注意命名是`authFilter`
+ Spring容器通过扫描`@Component`实例化`AuthFilter`

当`DelegatingFilterProxy`生效后，它会自动查找注册在`ServletContext`上的Spring容器，再试图从容器中找到名为`authFilter`的Bean，也就是我们用`@Component`声明的`AuthFilter`

## 3. 使用Interceptor

和Filter不同的是，Filter拦截的是整个后续处理流程，Interceptor仅针对Controller拦截，所以返回`ModelAndView`后，后续对View的渲染就脱离了Interceptor的拦截范围

```asciiarmor
┌─────────────────┐
│DispatcherServlet│<───┐
└─────────────────┘    │
 │              ┌────────────┐
 │              │ModelAndView│
 │              └────────────┘
 │ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ┐ ▲
 │    ┌───────────┐    │
 ├─┼─>│Controller1│──┼─┤
 │    └───────────┘    │
 │ │                 │ │
 │    ┌───────────┐    │
 └─┼─>│Controller2│──┼─┘
      └───────────┘
   └ ─ ─ ─ ─ ─ ─ ─ ─ ┘
```

使用Interceptor的好处是Interceptor本身是Spring管理的Bean，因此注入任意Bean都非常简单，还可以应用多个Bean，并且使用`@Order`来指定顺序

一个Interceptor必须实现`HandlerInterceptor`接口，可以选择实现`preHandle()`、`postHandle()`和`afterCompletion()`方法。分别在Controller方法调用前执行、Controller方法正常返回后执行、无论Controller方法是否抛异常都会执行，参数ex就是Controller方法抛出的异常，未抛出异常是`null`

在`preHandle()`方法中可以直接处理响应，然后返回`false`表示无需Controller方法继续处理了，通常在认证活着安全检查失败时直接返回错误响应。在`postHandle()`中，因为不活了Controller方法返回的`ModelAndView`，所以可以继续往`ModelAndView`里添加一些通用数据，很多页面需要的全局数据如Copyright信息等都可以放到这里，无需在每个Controller方法中重复添加

要让拦截器生效，需要在`WebMvcConfigurer`中注册所有的Interceptor

```java
@Bean
WebMvcConfigurer createWebMvcConfigurer(@Autowired HandlerInterceptor[] interceptors) {
    return new WebMvcConfigurer() {
        public void addInterceptors(InterceptorRegistry registry) {
            for (var interceptor : interceptors) {
                registry.addInterceptor(interceptor);
            }
        }
        ...
    };
}
```

```java
//在Controller中，SpringMVC还允许定义机遇@ExceptionHandler注解的异常处理方法
@Controller
public class UserController {
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleUnknowException(Exception ex) {
        return new ModelAndView("500.html", Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
    }
    ...
}
```

异常处理方法没有固定的签名，可以传入`Exception`、`HttpServletRequest`等，返回值可以是void，也可以是`ModelAndView`，尚需代码通过`@ExceptionHandler(RuntimeException.class)` 表示当发生`RuntimeException`时，就自动调用此方法处理

使用`@ExceptionHandler`，要注意它仅作用于当前的Controller，即ControllerA中定义的一个`@ExceptionHandler`方法对ControllerB不起作用，如果我们有很多Controller，每个Controller都需要一个处理一些通用的异常，如何避免重复代码

```java
@ControllerAdvice
public class InternalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ModelAndView handleUnknowException(Exception ex) {
    return new ModelAndView("500.html", Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
  }

}
```

## 4. 处理CORS

在开发REST应用时，很多时候，是通过页面的JavaScript和后端的RESTAPI交互

在JavaScript和REST交互的时候，有很多安全限制。默认情况下，浏览器按照同源策略放行JavaScript调用API，即：

+ 如果A站在域名`a.com`页面的JavaScript调用A站自己的API时，没有问题；
+ 如果A站在域名`a.com`页面的JavaScript调用B站`b.com`的API时，将被浏览器拒绝访问，因为不满足同源策略

同源策略要求域名完全相同`a.com`和`www.a.com`不同，协议相同Http和Https不同，端口号相同

可以利用CORS（跨域资源访问）来调用不同源的API，如果A站的JavaScript访问B站的API时，B站能够返回响应头`Access-Control-Allow-Origin: http://a.com`，那么浏览器就允许A站的JavaScript访问B站的API，可以看到决定权在提供API的服务方手中

关于CORS的详细信息可以参考[MDN文档](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Access_control_CORS)，一会看一下

使用`@ResrController`开发REST应用时，有以下几种方式设置

+ 使用`@CrossOrigin`

```java
//使用@CrossOrigin注解可以在@ResrController的class级别或者方法级别定义一个@CrossOrigin
@CrossOrigin(origins = "http://local.joeylee.com:8080")
@RestController
@RequestMapping("/api")
public class ApiController {}
```

上述的`@CrossOrigin`指定了只允许来自`local.joeylee.com`跨域访问，允许多个跨域访问要写成数组的形式，`@CrossOrigin(origins={"a.com","b.com"})`。如果要允许任何域的跨域访问写成`origins="*"`即可

如果有多个REST Controller都需要使用CORS，那么每个Controller都需要标注`@CrossOrigin`注解

+ 使用`CorsRegistry`

第二种方法是在`WebMvcConfigurer`中定义一个全局CORS配置

```java
@Bean
WebMvcConfigurer createWebMvcConfigurer(){
    return new WenMvcConfigurer(){
        @Override
        public void addCorsMappings(CorsRegistry registry){
            registry.addMapping("/api/**")
                  .allowedOrigins("http://local.joeylee.com:8080")
                  .allowedMethods("GET","PSOT")
                  .maxAge(3600);
            registry.addMapping("/rest/v2")
        }
    }
}
```

这种方式可以创建一个全局CORS配置，如果仔细地设计URL结构，那么可以一目了然地看到各个URL的CORS规则，推荐这种

## 5. 异步处理

SpringMVC如何实现异步请求进行异步处理的逻辑，首先是`web.xml`文件的不同

+ 不能再使用`<!DOCTYPE ...web-app_2_3.dtd">`的DTD声明，必须用新的支持Servlet 3.1规范的XSD声明，照抄即可；
+ 对`DispatcherServlet`的配置多了一个`<async-supported>`，默认值是`false`，必须明确写成`true`，这样Servlet容器才会支持async处理。

第一种async处理方式是返回一个`Callable`，SpringMVC自动把返回的`Callable`放入线程池执行，等待结果返回后再写入响应

```java
@GetMapping("/users")
public Callable<List<User>> users() {
    return () -> {
        // 模拟3秒耗时:
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        return userService.getUsers();
    };
}
```

第二种async方式是返回一个`DeferredResult`对象，然后在另一个线程中，设置此对象的值并写入响应

```java
@GetMapping("/users/{id}")
public DeferredResult<User> user(@PathVariable("id") long id) {
    DeferredResult<User> result = new DeferredResult<>(3000L); // 3秒超时
    new Thread(() -> {
        // 等待1秒:
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        try {
            User user = userService.getUserById(id);
            // 设置正常结果并由Spring MVC写入Response:
            result.setResult(user);
        } catch (Exception e) {
            // 设置错误结果并由Spring MVC写入Response:
            result.setErrorResult(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage()));
        }
    }).start();
    return result;
}
```

高性能异步IO的程序，会使用Netty框架

## 6. 使用WebSocket

WebSocket是一种基于HTTP的长连接技术，建立TCP连接后，浏览器发送请求时，附带几个头

```javascript
GET /chat HTTP/1.1
Host: www.example.com
Upgrade: websocket
Connection: Upgrade
```

表示客户端希望升级连接，变成长连接的WebSocket，服务器返回升级成功的响应

```XML
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
```

收到成功响应后表示WebSocket握手成功，代表WebSocket的这个TCP连接不会被服务器关闭，而是一直保持，服务器可随时向浏览器推送消息，浏览器也可随时向服务器推送消息，即可以是文本，也可以是二进制消息

在pom中加入如下依赖

+ org.apache.tomcat.embed:tomcat-embed-websocket:9.0.26
+ org.springframework:spring-websocket:5.2.0.RELEASE

第一项是嵌入式Tomcat支持WebSocket的组件，第二项是Spring封装的支持WebSocket的接口

在AppConfig中加入SpringWeb对WebSocket的配置，创建`WebSocketConfigurer`实例

```java
@Bean
WebSocketConfigurer createWebSocketConfigurer(
        @Autowired ChatHandler chatHandler,
        @Autowired ChatHandshakeInterceptor chatInterceptor)
{
    return new WebSocketConfigurer() {
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            // 把URL与指定的WebSocketHandler关联，可关联多个:
            registry.addHandler(chatHandler, "/chat").addInterceptors(chatInterceptor);
        }
    };
}
```

此实例在内部通过`WebSocketHandlerRegistry`注册能处理WebSocket的`WebSocketHandler`，以及可选的WebSocket拦截器`HandshakeInterceptor`。我们注入的这两个类都是自己编写的业务逻辑，后面我们详细讨论如何编写它们，这里只需关注浏览器连接到WebSocket的URL是`/chat`。

和处理普通HTTP请求不同，没法用一个方法处理一个URL。Spring提供了`TextWebSocketHandler`和`BinaryWebSocketHandler`分别处理文本消息和二进制消息，这里我们选择文本消息作为聊天室的协议，因此，`ChatHandler`需要继承自`TextWebSocketHandler`：

```JAVA
@Component
public class ChatHandler extends TextWebSocketHandler {
    ...
}
```

当浏览器请求一个WebSocket连接后，如果成功建立连接，Spring会自动调用`afterConnectionEstablished()`方法，任何原因导致WebSocket连接中断时，Spring会自动调用`afterConnectionClosed`方法，因此，覆写这两个方法即可处理连接成功和结束后的业务逻辑：

```JAVA
@Component
public class ChatHandler extends TextWebSocketHandler {
    // 保存所有Client的WebSocket会话实例:
    private Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 新会话根据ID放入Map:
        clients.put(session.getId(), session);
        session.getAttributes().put("name", "Guest1");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        clients.remove(session.getId());
    }
}
```

每个WebSocket会话以`WebSocketSession`表示，且已分配唯一ID。和WebSocket相关的数据，例如用户名称等，均可放入关联的`getAttributes()`中。

用实例变量`clients`持有当前所有的`WebSocketSession`是为了广播，即向所有用户推送同一消息时，可以这么写：如果要推送给指定的几个用户，那就需要在`clients`中根据条件查找出某些`WebSocketSession`，然后发送消息。

注意到我们在注册WebSocket时还传入了一个`ChatHandshakeInterceptor`，这个类实际上可以从`HttpSessionHandshakeInterceptor`继承，它的主要作用是在WebSocket建立连接后，把HttpSession的一些属性复制到WebSocketSession，例如，用户的登录信息等：

```JAVA
@Component
public class ChatHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    public ChatHandshakeInterceptor() {
        // 指定从HttpSession复制属性到WebSocketSession:
        super(List.of(UserController.KEY_USER));
    }
}
```

这样，在`ChatHandler`中，可以从`WebSocketSession.getAttributes()`中获取到复制过来的属性。
