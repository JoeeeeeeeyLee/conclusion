# RELEASE

两种方式一种是上传的时候直接上传上传链接，一种是上传的时候只上传links到对应的release中

**前提:**

+ release命名:v1.0-release
+ Project-id:1516
+ tag命名v1.0
+ token：每个人都不一样
+ links地址：选择你放制品的地址

**注意:**

不同的links的网址是不可以相同的否则会报下面的错误

```{"message":{"url":["has already been taken"]}}%```

+ 第一种

```bash
curl --header 'Content-Type: application/json' --header "PRIVATE-TOKEN: " \
--data '{ "name": "v1.0-release", "tag_name": "v1.0", "description": "测试release发布功能", "assets": { "links": [{ "name": "git-toolkit_darwin_amd64", "url": "https://url/v2.0/git-toolkit_darwin_amd64" }] } }' \
--request POST https://url/api/v4/projects/1516/releases
```

+ 第二种

```bash
curl --header "PRIVATE-TOKEN:" \
--data name="git-toolkit_linux_amd64" \
--data url="https://url/git-toolkit_linux_amd64" \
--request POST https://url/api/v4/projects/1516/releases/v1.0/assets/links
```
