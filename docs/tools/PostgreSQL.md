# Mac安装PostgreSQL

## 安装

```bash
brew search postgresql
brew info postgresql
brew install postgresql -v
```

### 初始化

```bash
initdb /usr/local/var/postgres
报错
initdb: error: directory "/usr/local/var/postgres" exists but is not empty If you want to create a new database system, either remove or empty the directory "/usr/local/var/postgres" or run initdb with an argument other than "/usr/local/var/postgres".
```

使用如下命令将已经存在的目录删除，`rm -rf /usr/local/var/postgres`

```bash
启动服务
pg_ctl -D /usr/local/var/postgres -l logfile start
关闭服务
pg_ctl -D /usr/local/var/postgres -l logfile stop
```

### 创建数据库和账户

mac需要自己创建同名数据库

`CREATEDB`

- 登陆psql控制台

`psql`

使用`\l`查看所有的数据库，可以看到有一个和当前用户名同名的数据库，还有一个postgres数据库，postgres数据库的所有者是当前用户，而不是postgres

- 创建postgres用户

```bash
 CREATE USER postgres WITH PASSWORD '123456';
```

- 删除数据库

```bash
 DROP DATABASE postgres;
```

- 创建属于postgres用户的postgres数据库

```bash
 CREATE DATABASE postgres OWNER postgres;
```

- 将数据库所有权限赋予postgres用户

```undefined
 GRANT ALL PRIVILEGES ON DATABASE postgres to postgres;
```

- 给postgres用户添加创建数据库的属性

```undefined
 ALTER ROLE postgres CREATEDB;
```

### 登录控制台指令

```css
psql -U [user] -d [database] -h [host] -p [post]
```

-U指定用户，-d指定数据库，-h指定服务器，-p指定端口

上方直接使用`psql`登录控制台，实际上使用的是缺省数据

```undefined
user：当前mac用户
database：用户同名数据库
主机：localhost
端口号：5432，postgresql的默认端口是5432
```

完整的登录命令，比如使用postgres用户登录

```undefined
psql -U postgres -d postgres
```

### 常用控制台命令

```css
\password：设置当前登录用户的密码
\h：查看SQL命令的解释，比如\h select。
\?：查看psql命令列表。
\l：列出所有数据库。
\c [database_name]：连接其他数据库。
\d：列出当前数据库的所有表格。
\d [table_name]：列出某一张表格的结构。
\du：列出所有用户。
\e：打开文本编辑器。
\conninfo：列出当前数据库和连接的信息。
\password [user]: 修改用户密码
\q：退出
```

## postgresql  schema

`schema`概念类似于命名空间或者文件目录，唯一的区别就是`schema`不许有`schema`嵌套。各个表对象视图存放在不同的`schema`下，同一个`schema`下不许有重复的对象的名字，不同的`schema`下则可以重复

使用`schema`的作用

- 方便管理多个用户可以共享一个数据库，但是schema又是相互独立的
- 方便管理众多的对象，更有逻辑性
- 方便兼容某些第三方应用程序，创建对象时是有schema的

```sql
create schema schema1;
create table schema1.user();
drop schema schema1;
```
