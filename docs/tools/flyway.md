# Flyway

## 1. WHAT

Flyway是一款开源的数据库版本管理工具，它更倾向于规约优于配置的方式。Flyway可以独立于应用实现管理并跟踪数据库变更，支持数据库版本自动升级，并且有一套默认的规约，不需要复杂的配置，Migrations可以写成SQL脚本，也可以写在Java代码中，不仅支持Command Line和Java API，还支持Build构建工具和Spring Boot等，同时在分布式环境下能够安全可靠地升级数据库，同时也支持失败恢复等。

flyway主要有`Metadata`表和`Migrate`,`Clean`,`Info`,`Validate`,`BaseLine`,`Repair`组成

## 2. WHY

在真实项目中，由于有多个开发人员和多个开发环境，比如`pro`,`test`,`ci`,`local`等等，当管理几个环境时意味着这是一个挑战，在代码管理方面由于越来越多的版本控制工具，以及持续交付和定义好的发布和部署流程，可以很好的解决这一个问题，但是在数据库方面这个问题并没有很好的解决，比如多人开发时，可能有人会修改你的表的某个字段，但是没有告诉你，或者要添加一个新表。那么我们如何能知道当前机器上的数据库是处于一个什么状态，新的表的sql脚本是否也在你的机器上执行过呢，对表上的一点小的更改，你的机器上是否也进行了更改呢。对于这些我们无从得知，这也正是`Flyway`的意义所在

## 3. HOW

`Flyway`对数据库进行版本管理主要由`Metadata`表和`Migrate`,`Clean`,`Info`,`Validate`,`BaseLine`,`Repair`完成

### Metadata Table

`Flyway`中最核心的就是用于记录所有版本演化和状态的Metadata表，在`Flyway`首次启动时会创建默认名为`flyway_schema_history`的元数据表，其表结构为(以Postgresql为例)：

![metadata](../imgs/flyway/metadata.png)

### Migrate

将schema迁移成最新版本，schema历史表如果不存在flyway会自动创建，`Migrate`是flyway中的核心组件，它将会扫描文件系统或者`classpath`来寻找可以可以迁移的sql，比对版本号，如果有任何差异存在，则依次迁移。通常应该在程序启动时执行Migrate操作，从而避免数据库和期望的不同

![command-migrate](../imgs/flyway/command-migrate.png)

### Clean

即清除掉对应数据库Schema中的所有对象，包括表结构，视图，存储过程，函数以及所有的数据等都会被清除。

不要在生产环境中使用

![command-clean](../imgs/flyway/command-clean.png)

### Info

看到哪些迁移已经执行，哪些还在等待中，何时执行以及是否成功

![command-info](../imgs/flyway/command-info.png)

### Validate

`Validate`的原理是对比Metadata表与本地`Migrations`的`CheckSum`值，如果值相同则验证通过，否则验证失败，这主要是防止已经迁移到数据库的Migrations的修改

![command-validate](../imgs/flyway/command-validate.png)

### Undo

![command-undo](../imgs/flyway/command-undo.png)

如果`target`指定，则Flyway将尝试按应用的顺序撤消版本化的迁移，直到它击中目标版本以下的版本。如果`group`处于活动状态，Flyway将尝试在单个事务中撤消所有这些迁移。

如果没有到undo的版本迁移，则调用undo无效。

没有可重复迁移的撤消功能。在这种情况下，可重复的迁移应修改成包括较旧的状态的一个愿望，然后使用重新应用迁移

undo可能经常会崩溃

### Baseline

Baseline针对已经存在Schema结构的数据库的一种解决方案，即实现在非空数据库中新建Metadata表，并把Migrations应用到该数据库。

基准是通过将特定数据库作为基准对Flyway引入现有数据库。这将导致`Migrate`忽略直到基线版本（包括基线版本）的所有迁移。然后，将照常应用较新的迁移。

![command-baseline](../imgs/flyway/command-baseline.png)

### Repair

修复是用于修复`Metadata`表问题的工具。它有两个主要用途：

- 删除失败的迁移记录（仅适用于不支持DDL事务的数据库）
- 将可用迁移的`CheckSum`，描述和类型与可用迁移重新对齐,比如：某个Migratinon已经被应用，但本地进行了修改，又期望重新应用并调整Checksum值，不过尽量不要这样操作，否则可能造成其它环境失败。

### 4. Create Migration

**Migrations**是指Flyway在更新数据库时是使用的版本脚本，比如：一个基于Sql的Migration命名为`V1__init_tables.sql`，内容即是创建所有表的sql语句，另外，Flyway也支持基于Java的Migration。Flyway加载Migrations的默认Locations为`classpath:db/migration`，也可以指定`filesystem:/project/folder`，其加载是在Runtime自动递归地执行的。

![sql_migration_base_dir](../imgs/flyway/sql_migration_base_dir.png)

除了需要指定Location外，Flyway对Migrations的扫描还必须遵从一定的命名模式，Migration主要分为两类：Versioned和Repeatable。

- **Versioned migrations**
    一般常用的是Versioned类型，用于版本升级，每一个版本都有一个唯一的标识并且只能被应用一次，并且不能再修改已经加载过的Migrations，因为Metadata表会记录其Checksum值。其中的version标识版本号，由一个或多个数字构成，数字之间的分隔符可以采用点或下划线，在运行时下划线其实也是被替换成点了，每一部分的前导零会被自动忽略。
- **Repeatable migrations**
    Repeatable是指可重复加载的Migrations，其每一次的更新会影响Checksum值，然后都会被重新加载，并不用于版本升级。对于管理不稳定的数据库对象的更新时非常有用。Repeatable的Migrations总是在Versioned之后按顺序执行，但开发者必须自己维护脚本并且确保可以重复执行，通常会在sql语句中使用`CREATE OR REPLACE`来保证可重复执行。

默认情况下基于Sql的Migration文件的命令规则如下图所示：

![sql_migration_naming](../imgs/flyway/sql_migration_naming.png)

其中的文件名由以下部分组成，除了使用默认配置外，某些部分还可自定义规则。

- prefix: 可配置，前缀标识，默认值`V`表示Versioned，`R`表示Repeatable
- version: 标识版本号，由一个或多个数字构成，数字之间的分隔符可用点`.`或下划线`_`
- separator: 可配置，用于分隔版本标识与描述信息，默认为两个下划线`__`
- description: 描述信息，文字之间可以用下划线或空格分隔
- suffix: 可配置，后续标识，默认为`.sql`

### 5. Use

在SpringBoot下使用，添加依赖，配置文件中进行配置

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>${flyway.version}</version>
</dependency>
```

```yaml
spring:
    flyway:
        baseline-description: baseline init
        baseline-on-migrate: true
        Schema: public
        locations: classpath:db/migration/
        baseline-version: 1.0.101

spring:
    flyway:
        enabled: true
```

### 6. Conclusion

原理

1. 项目启动时拉起`Flyway`，先检查数据库里面有没有`Flyway`元数据表，没有则创建；
2. 检查`Flyway`元数据表中的记录，哪些脚本已经执行过，当前版本是什么；
3. 查找代码中的(名称满足规则的)数据库升级脚本，找出版本号大于(`Flyway`元数据)当前版本的脚本，逐个执行并记录执行结果到`Flyway`元数据表。

通过以上功能，我们可以很容易做到：

1. 代码与数据库建表&升级脚本放在一起同步管理，通过代码(SQL)就可以了解到表结构；
2. 无须人工执行任何脚本，运行代码或服务即可完成(数据库表结构的)环境搭建；
3. 从任一版本的环境(表结构)，都可以通过运行指定(新)版本的代码或服务来自动升级到指定新版本；
4. (配合内存数据库/Docker/清库脚本)数据库搭建&升级脚本很容易与代码一起反复测试。

### 7. References

<https://flywaydb.org/documentation/>

<https://my.oschina.net/u/4254626/blog/3179612>

<https://blog.waterstrong.me/flyway-in-practice/>
