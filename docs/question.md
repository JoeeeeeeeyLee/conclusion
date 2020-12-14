# 开发过程中遇到的问题

## 1

```go
// get all authors
func GetAllAuthor() []Author {
    args := []string{"shortlog", "-s", "-n", "-e", "--no-merges"}
    var authors []Author

    info := utils.MustExecRtOut(Cmd, args...)

    lines := strings.Split(info, "\n")
    for _, line := range lines {
        if len(strings.TrimSpace(line)) > 0 {
            author := GetAuthor(line)
            authors = append(authors, author)
        }
    }
    return authors
}

// get information about an author
func GetAuthor(line string) Author {
    authorInfo := strings.Fields(strings.TrimSpace(line))
    emailIndex := len(authorInfo) - 1

    email := strings.TrimSpace(authorInfo[emailIndex])
    email = utils.SubString(email, 1, len(email)-1)
    author := strings.Join(authorInfo[1:emailIndex], " ")
    number, err := strconv.Atoi(authorInfo[0])

    if err != nil {
        fmt.Printf("%s parse commit number %s error!\n", line, authorInfo[0])
    }

    return Author{
        Name:         author,
        Email:        email,
        CommitNumber: number,
    }

}
```

在之前的调用中无论是debug还是在main函数中调用都是正常的，但是当在单元测试中调用这个方法就会不正常，这个问题困扰好长时间

最后在这里查到了
>[解答](https://stackoverflow.com/questions/51966053/what-is-wrong-with-invoking-git-shortlog-from-go-exec)  
If no revisions are passed on the command line and either standard input is not a terminal or there is no current branch, git shortlog will output a summary of the log read from standard input, without reference to the current repository.

也就是当我们在单元测试中进行运行时，我们需要在命令的后面加上指定的版本名，或者分支名，这时候才可以正常显示，否则的话不会进行输出，当然我们也可以在终端直接输出

## 2

```bash
parse : net/url: invalid control character in URL
```

```go
func GetProjectName() string {
    dir := ExecRtOut(Cmd, "rev-parse", "--show-toplevel")
    _, projectName := filepath.Split(dir)
    return projectName
}
```

报错如上，调试发现是因为GetProjectNamm方法，返回的projectName后面还有一个空格，导致解析URI的时候出错

## 3

```bash
json: cannot unmarshal array into Go value of type UserInfo
```

这是因为uri返回的值是一个数组，不能将数组直接绑定到UserInfo类型上，而需要一个UserInfo类型的数组，修改代码如下

```go
    var userInfoByte []byte
    if response, err := http.Get(url); err != nil {
        fmt.Println(err)
    } else {
        if userInfoByte, err = ioutil.ReadAll(response.Body); err != nil {
            fmt.Println(err)
        }
    }
    var u []UserInfo
    err := json.Unmarshal(userInfoByte, &u)
```

## 4

记录Golang中使用os.MkdirAll()的相关权限问题

```Golang
func main() {
    err:=os.MkdirAll("test1/test2/test3",0666)
    if err!=nil {
        fmt.Println(err)
    }
}
```

运行结果`mkdir test1/test2: permission denied`
此时test1目录正常创建，可是接下来的test2目录创建失败
终端ll查看当前目录下面的文件以及目录
`drw-r--r--  2  staff    64B Nov 19 11:21 test1`
10个字符d代表目录，后面9个三个一组分别代表所有者、同组用户和其他用户

我们创建文件夹的时候指定的权限是0666，拆分成2进制，也就是110110110,也就是rw-rw-rw-，但是为什么我们创建的文件夹是rw-r--r--呢，这是因为我们在创建文件的时候会系统将我们赋予的权限自动减去umask值，终端输入umask，输出为022，这代表，同组用户和其他用户都要减去十进制的2,也就变成了110100100

我们尝试cd到test1目录下，报错`cd: permission denied: test1`，这是因为cd到一个目录，需要这个目录的执行权限也就是x权限，所以我们想要修改这个目录，就必须拥有这个目录的执行权限

所以这也是766的必要行所在

## 5. 利用api接口获取ProjectID和UserID

```golang
// 用户信息
type UserInfo struct {
    ID        int    `json:"id"`
    Name      string `json:"name"`
    Username  string `json:"username"`
    State     string `json:"state"`
    AvatarUrl string `json:"avatar_url"`
    WebUrl    string `json:"web_url"`
}

type ProjectInfo struct {
    ID        int    `json:"id"`
    Name      string `json:"name"`
}

// 调用api获取username对应的user id
func FetchUserInfo(username string) []UserInfo {
    url := GetProjectUrl() + "/users?username=" + username
    var userInfoByte []byte
    if response, err := http.Get(url); err != nil {
        fmt.Println(err)
    } else {
        if userInfoByte, err = ioutil.ReadAll(response.Body); err != nil {
            fmt.Println(err)
        }
    }
    var u []UserInfo
    err := json.Unmarshal(userInfoByte, &u)
    if err != nil {
        log.Fatal(err)
    }
    return u

}

// 获取项目信息
func FetchProjectInfo() []ProjectInfo {
    projectName := strings.TrimSpace(GetProjectName())
    url := GetProjectUrl() + "/projects?search=" + projectName
    var projectInfoByte []byte
    if response, err := http.Get(url); err != nil {
        fmt.Println(err)
    } else {
        if projectInfoByte, err = ioutil.ReadAll(response.Body); err != nil {
            fmt.Println(err)
        }
    }
    var p []ProjectInfo
    err:=json.Unmarshal(projectInfoByte,&p)
    if err != nil {
        log.Fatal(err)
    }
    return p
}

//获取项目ID，URL对大小写不敏感
func GetProjectID() int {
    projectInfo := FetchProjectInfo()
    var targetProjectID int
    for _, project := range projectInfo {
        if project.Name == strings.TrimSpace(GetProjectName()) {
            targetProjectID = project.ID
        }
    }
    return targetProjectID
}
```
