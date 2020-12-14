# 总结

今天完成了自己的第一个项目，想写一点总结

## 1. 最初的开始——不知道干什么，跟别提怎么干了

最初接到的任务其实是访问控制限制，即设计一个白名单功能，让在白名单中的人才能实现对接口的访问，具体准备工作以及相关调研在另一篇文档中，最终是决定利用nginx，但是之前的项目中这块的工作可以直接复用，也就意味着不需要我开发这块的功能，后来就被分配设置指挥中心，但是呢由于项目还没有立项，以及文档的不清楚，我收到了两个文档，一对照，发现我这个功能不是一个小任务，跟我涛哥说的设置一条数据就可以怎么不太一样啊，还要自己写HTTPS访问，然后我就去看Okhttp的使用，怎么建立这种，和产品越聊任务越多，马上快要爆炸了整个人，这时候涛哥回来了，有定海神针了，给我找了项目的负责人问了一下我的流程是怎么样的，明确了我的具体需求，只需要设置指挥中心接口，并有一个获取指挥中心信息的接口

这一过程的体验不得不说是很差的，总结起来原因无非以下几点

+ 我和产品我俩在平行时空聊着，鸡同鸭讲，以后要知道一定要明确需求，而且必须找个开发技术人员聊，可以是涛哥，或者是项目开发方面负责的人聊这个任务，因为产品不懂技术，我现在又是个半吊子，所以我俩就完全在那云聊。
+ 我以为产品说的我懂了，产品以为我说的他懂了，但其实都不懂。。。。
+ 接口的设计出了很多问题导致一直修改，解决的办法应该是多看其他的项目中的接口设计，照猫画虎
+ **明确需求 需求  需求！！！！！！！！！！！！！**

## 2. 曲折的中间——知道干啥了，但不知道怎么干

+ 一开始需要打桩，先把打包在Jenkins集成，但是之前并不知道什么是Jenkins，就一步步的问，挨个文件从别的项目里拷过来，再对应的更改
+ Jenkins整完以后，就是实际开发，不会啊，怎么整呢，幸亏刚哥直接上传了一份资产的demo，我就照着demo写，因为到这之前，我已经写了一些，比如我写了一些自己需要用到的异常类，但是在真正的项目中，是不应该这样的，而应该给出准确的描述，并按照公司的规范，所以一般都是错误码的形式展现，这一点就导致之前写的抛出异常的代码都白写了
+ 同样VO，DTO，PO之间的数据转换，我是不需要PO的，但是因为demo中有，所以我就照着写了，但是最后都是无效的代码，全部delete，只保留了要用的VO和DAO
+ 以及对枚举的使用，之前在设计的时候还在想我这个是枚举值，我应该怎么存值的类型，后来涛哥说可以设计成每个枚举值代表一个数字，这样即表示了状态又能减少数据库的开销

## 3. 光明的未来——基本知道怎么干，但是有瑕疵

最后遇到了返回值不对，逻辑不对，部署不对，但是在我的不停追问之下还好都解决了。Nice Joe

下面是收获

1.枚举类的使用

之前指挥简单定义枚举，但是存数据库时就在想如何存这三个状态字段，还在考虑是varchar还是什么，但是现在学到一种新的方法，利用枚举类的构造方法，将对应的枚举值转换成对应的整数值，这样存数据的时候，选择类型直接可以smallint，实现存储空间的节省，以及实现上的漂亮。在定义枚举类的时候只需要如下操作即可

```java
public enum Status{
    good(1),
    bad(2);
    public static Map<Integer,Status> lookup=new HashMap<>();
    //静态内部类，将状态与对应的值存放入map中
    static{
        for(Status  s :Status.values()){
            lookup.put(s.getValue(),s);
        }
    }
    private Integer value;
}
```

2.跑单元测试时出现如下错误，因为单纯跑了一个`assertTrue(Objects.isNull(null))`所以断定其他的没什么问题，问题出在数据库上面

```nested exception is org.flywaydb.core.api.FlywayException: Validate failed.Migration checksum mismatch for migration version 1.0.101```

+ 上网查找资料，第一种有人说这是因为flyway的版本太低，改成4.0，一看自己的版本都已经6.0，明显不是
+ 第二种有人说使用`mvn flyway:migrate`，显示错误连接不上数据库，请配置用户名和密码，找到错误了,这是因为，我的配置文件`applicationl-local.xml`文件中没有配置当前开发环境的postgres地址，在其中配置本地的postgres数据库即可

1.当我们设置实体类的时候，int和Integer是有很大区别的，比如下面

```java
private int a;  //默认值是0
private Integer b;  //默认值是null，所以请求的时候不赋值的话数据库中这个值就是null
//如果你希望他有一个默认值，那就应该在开始的时候给b一个默认值，所以正确的写法是这样的
private Integer b = 0;
```

这样在你传参数xyz的时候，就不仅光显示xyz的值，而会把b的默认值也带上，这样就实现了简单的初始化的功能，不需要像之前一样使用setter来对b进行设置

3.我在项目中建立了一张表，这个表的主键是一个id字段，但是我这个id字段和一般表的id字段不同，我这个是一个明确的id地址，我建表的时候讲这个id设置为主键，因为需求是我这个数据库中只能存一条记录，所以在插入数据库之前需要先查，所以思路是

+ 每次插入数据时，先查，如果没有则直接插入
+ 如果已经存在一条数据了，那么此时也有两种做法
+ 一种是直接将那条数据删除，然后再插入
+ 另一种就是直接更新这条数据

在这个项目中我使用的就是第二种方法，在第一种没有数据存在的情况下，所有的接口都是正常的，但是在第二种有数据的情况下，就会导致我设置的时候数据返回的是正常的，但是在获取当前的信息的时候，会还是获取到之前存在的那一条记录，查看impl以及inte都是正常的，debug之后发现是更新语句没有成功，查了一会发现是sql语句写错了，下面的是我之前的sql语句

```xml
<update id="updateCommandCenterInfoById" parameterType="dao.entity.CommandCenterEntity">
    update command_center
    <set>
        <if test="commandCenterEntity.ip != null">
            ip=#{commandCenterEntity.ip,jdbcType=VARCHAR},
        </if>
        <if test="commandCenterEntity.port != null">
            port=#{commandCenterEntity.port,jdbcType=INTEGER},
        </if>
        <if test="commandCenterEntity.registrationStatus!=null">
            registration_status=#{commandCenterEntity.registrationStatus,jdbcType=SMALLINT},
        </if>
        <if test="commandCenterEntity.certificationStatus!=null">
            certification_status=#{commandCenterEntity.certificationStatus,jdbcType=SMALLINT},
        </if>
        <if test="commandCenterEntity.connectionStatus!=null">
            connection_status=#{commandCenterEntity.connectionStatus,jdbcType=SMALLINT},
        </if>
    </set>
    where id=#{commandCenterEntity.id,jdbcType=VARCHAR}
</update>
```

如果我的id不是这种带有特殊意义的id这样写是没错的，但是我的id是主键，我更新的时候需要将我id也更新成新设置的id，而我并没有写出更新id的语句，更改为下面的sql语句，一切恢复正常

```xml
<update id="updateCommandCenterInfoById" parameterType="dao.entity.CommandCenterEntity">
    update command_center
    <set>
        <if test="commandCenterEntity.ip != null">
            ip=#{commandCenterEntity.ip,jdbcType=VARCHAR},
        </if>
        <if test="commandCenterEntity.port != null">
            port=#{commandCenterEntity.port,jdbcType=INTEGER},
        </if>
        <if test="commandCenterEntity.registrationStatus!=null">
            registration_status=#{commandCenterEntity.registrationStatus,jdbcType=SMALLINT},
        </if>
        <if test="commandCenterEntity.certificationStatus!=null">
            certification_status=#{commandCenterEntity.certificationStatus,jdbcType=SMALLINT},
        </if>
        <if test="commandCenterEntity.connectionStatus!=null">
            connection_status=#{commandCenterEntity.connectionStatus,jdbcType=SMALLINT},
        </if>
        <if test="commandCenterEntity.id!=null">
            id=#{commandCenterEntity.id,jdbcType=VARCHAR}
        </if>
    </set>
</update>
```
