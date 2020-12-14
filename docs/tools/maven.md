# maven换源

之前电脑上是通过brew安装的maven，但是换源之后下载依赖的速度忽快忽慢，索性直接```brew uninstall maven```，然后到maven官方下载压缩包，解压之后放到```/usr/local/maven```下面，然后配置环境变量，`vim ~/.bash_profile`在里面添加

```xml
vim ~/.bash_profile
export M2_HOME=/usr/local/maven/apache-maven-3.6.3
export PATH=$M2_HOME/bin:$PATH
source ./bash_profile
```

然后新建maven项目，右键maven项目打开`settings.xml`在其中添加如下代码

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>aliyunmaven</id>
            <mirrorOf>central</mirrorOf>
            <name>阿里云公共仓库</name>
            <url>https://maven.aliyun.com/repository/central</url>
        </mirror>
        <mirror>
            <id>repo1</id>
            <mirrorOf>central</mirrorOf>
            <name>central repo</name>
            <url>http://repo1.maven.org/maven2/</url>
        </mirror>
        <mirror>
            <id>aliyunmaven</id>
            <mirrorOf>apache snapshots</mirrorOf>
            <name>阿里云阿帕奇仓库</name>
            <url>https://maven.aliyun.com/repository/apache-snapshots</url>
        </mirror>
    </mirrors>
</settings>
```
