# 错误总结

1.跑单元测试时出现如下错误，因为单纯跑了一个`assertTrue(Objects.isNull(null))`所以断定其他的没什么问题，问题出在数据库上面

```nested exception is org.flywaydb.core.api.FlywayException: Validate failed.Migration checksum mismatch for migration version 1.0.101```

+ 上网查找资料，第一种有人说这是因为flyway的版本太低，改成4.0，一看自己的版本都已经6.0，明显不是
+ 第二种有人说使用`mvn flyway:migrate`，显示错误连接不上数据库，请配置用户名和密码，找到错误了,这是因为，我的配置文件`applicationl-local.xml`文件中没有配置当前开发环境的postgres地址，在其中配置本地的postgres数据库即可

2.当我们设置实体类的时候，int和Integer是有很大区别的，比如下面

```java
private int a;//默认值是0
private Integer b;//默认值是null，所以请求的时候不赋值的话数据库中这个值就是null
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
