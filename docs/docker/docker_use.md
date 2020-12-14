# Docker学习

## 一.docker基本命令

1.查看是否正确安装
`docker version`

2.查看docker所有可以使用的参数
`docker`

3.搜索想要使用的镜像
`docker search <image-name>`

4.拉去想要使用的镜像
`docker pull <image-name>`

5.在容器中安装程序
`docker run <image-name> apt-get install -y ping`
使用的apt-get命令是基于操作系统来使用的，apt-get命令要带上-y参数，如果不指定会进入到交互模式中，需要用户输入命令来确认，但是docker容器中则无法响应这种交互

6.保存对容器的修改
当通过运行命令，对容器进行修改以后，可以将对容器的修改保存下来，这样下次可以从保存后的最新状态运行该容器，这一过程称之为committing，他保存新旧状态的区别

```bash
docker ps -l
docker commit <CONTAINER ID>
```

7.检查运行中的镜像
`docker ps` 查看所有的运行中的镜像
`docker inspect <CONTAINER ID>`查看某个具体容器的信息

8.发布自己的镜像
`docker images`查看所有安装过的镜像
`docker push <REPOSITORY>`将镜像推送到自己的docker空间下

## 二.docker运行postgresql

1.使用docker运行pg

 ```bash
1.运行镜像
docker run --name pg -v /tmp/data/pgdata/:/var/tmp/postgresql/data -e POSTGRES_USER='postgres' -e POSTGRES_PASSWORD='' -d -p 5432:5432 -t postgres

2.显示容器
docker container ls

3.进入到容器中
docker exec -it containerId bash

4.修改配置文件
cd /var/tmp/postgresql/data
apt-get -y install vim
apt-get update
apt-get -y install vim
vim postgresql.conf
#修改：在所有IP地址上监听，从而允许远程连接到数据库服务器：
listening_address: '*'

vim pg_hba.conf
#添加或修改：允许任意用户从任意机器上以密码方式访问数据库，把下行添加为第一条规则：
host    all             all             0.0.0.0/0               md5

5.连接数据库
psql -U postgres -W
更改编码方式
update pg_database set encoding=pg_char_to_encoding\('UTF8'\) where datname='basemap'
