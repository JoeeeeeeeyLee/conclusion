# Git分布式版本控制工具

## 前序操作

1.将本地库和远程库关联起来，输入如下命令

```bash
git remote add origin http
```

2.当远程库是新建的时候，需要推送本地master分支时，需要加上-u参数，Git不但会把本地master分支的内容推到远程的master分支上，还会把本地的master分支和远程的master分支关联起来。

```bash
git push -u origin master
```

3.以后的推送代码可以直接使用```git push origin master```

## add和commit

1. 首先我们需要初始化git仓库使用```git init``命令，此时会自动创建.git隐藏文件夹和master分支
2. 如果我们对当前分支尽心了修改比如添加文件或者删除文件，我们需要知道添加或者删除的是哪一个文件，可以使用```git status```命令
3. 如果我们对修改结果没有意见，我们可以使用```git add ./文件名```挨个添加、或者直接使用```git add .```一次性全部添加、或者使用```git add ./*.md```通配符添加。add后文件是处于暂存区中的，而我们想让它到达远程分支中，还需要```git commit -m '注释提交的意义'```命令。

++++

## 版本回退与删除

1. 当我们提交了多次以后，可能想看一下历史提交记录，此时可以使用```git log```命令，会从上到下由近及远显示所有的历史提交记录。```git log -p```会显示历史提交详情。如果想回退到上一个版本可以使用```git reset --hard HEAD^```，如果想回退到上上一个版本可以使用HEAD^^。
2. 假如又想回退到最新的版本那么可以使用```git reset --hard 版本号```的形式，查询所有历史记录的版本号可以使用```git reflog```命令，然后选择想要回退的版本即可。
3. 取消暂存区中的某个文件```git reset Head 文件名```，取消暂存区中的所有文件```git reset Head .```和add所有的用法是相似的
4. 移除工作目录中的某个文件```git clean -f 文件名```，移除所有的```git clean -df```，当没commit之前可以使用```git checkout 文件名```，以及恢复所有的文件```git checkout .```

++++

## 查看不同

+ 查看没提交到暂存的文件可以使用```git diff```命令
+ 查看已经提交到暂存区的文件可以使用```git diff --cached```

## 分支切换与合并

1. 切换分支```git checkout branch```
2. 查看分支```git branch```
3. 新建并切换分支```git checkout -b branch```
4. 合并branch到当前分支上```git merge branch```
5. 删除分支```git branch -d branch```

## 标签的使用

1. 新建tag```git tag -a version -m ''```
2. 获取tag列表```git tag -l```
3. 查看具体tag```git show tag```
4. 删除tag```git show -d tag```

## GIT扩展

实现一系列流程

```bash
git clone
git fetch origin develop
git checkout -b develop
git pull origin develop
```

当你发现你的文件夹中有的文件在一次性全部提交的时候没有提交上去，这可能是因为你在.gitignore文件中对其进行配置，或者是在一开始的时候你没有将他加入到缓存域中，这时候可以使用`git add script/bin/* -f`使用`-f`参数强制提交。

`.gitignore`文件的编写规则

```bash
# 注解.gitignore文件中的注解
*.a  忽略所有以.a结尾的文件
!lib.a 但lib.a除外
/TODO 仅仅忽略根目录下的TODO文件，不包括次级目录下的TODO文件
build/ 忽略build/目录下的所有文件
doc/*.txt 会忽略doc/a.txt 但不会忽略doc/sub/a.txt文件
```

但有时想将目录中的某些文件加入到.gitignore文件中，但在`.gitignore`文件中定义了也不好使，这是因为`.gitignore`文件只能忽略那些原本没有被`track`的文件，如果某些文件已经被纳入了版本管理中，那么修改`.gitignore`文件是无效的，方法就是将本地缓存删除(改变成未track的状态)，然后再提交

```bash
git rm -r --cache .
git add .
git commit -m 'update .gitignore'
```

git pull的时候报错如下

```bash
error: Your local changes to the following files would be overwritten by merge: xxx/xxx/xxx.php Please, commit your changes or stash them before you can merge. Aborting
```

这是因为本地已经有修改，同时别人修改了代码并且提交到版本库中去了。

三种方式

+ 使用`git stash`将工作区恢复到上次提交的样子，然后`git pull`拉取代码，再`git stash pop`将之前修改的代码恢复到当前工作区。
+ `git reset --mixed HEAD^`不删除工作空间的代码，撤销commit，撤销add，等同`git reset HEAD^`
+ `git reset --soft HEAD^`不删除工作空间的代码，撤销commit，但不撤销add
+ `git reset --hard HEAD^`删除工作空间的代码，撤销commit，也撤销add

当想要获取远程的所有信息包括`tag`，同时想要删除本地所有不在远程的分支，可以使用`git fetch --prune`

## MAVEN

查看自己的代码是否符合规范

`mvn install -DskipTests -U`

`mvn flyway:migrate`

将jar包打进本地maven仓库

```bash
mvn install:install-file -Dfile= -DgroupId= -DartifactId= -Dversion= -Dpackaging=
```
