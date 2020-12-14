# GraphQL Learn

## First Part

### 1.什么是GraphQL

GraphQL是一个用于API的查询语言，通过使用一个由你的数据定义的类型系统在服务端运行时执行查询。GraphQL并没有和任何特定数据库或者存储引擎绑定，而是依靠你现有代码和数据支撑。

一个GraphQL服务是通过定义类型和类型上的字段来创建的，然后给每个类型上的每个字段提供解析函数。一旦一个GraphQL服务运行起来（通常在web服务的一个URL上），它就能就收GraphQL查询，并验证和执行。接收到的查询首先会被检查确保它只引用了已定义的类型和字段，然后运行指定的解析函数来生成结果。

### 2.GraphQL的优点

+   GraphQL有强类型schema，比如后端api更新了，但文档没有修改，所以不知道新的api是干什么的。GraphQL schema是每个GraphQL API的基础，它清晰的定义了每个api支持的操作，包括输入的参数和返回的内容
+   你只会获取到你请求的数据，比如一本书有作者出版社，当你只查询书的作者的时候，那就只会显示作者数据。
+   schema可以进行拼接，可以组合和连接多个GraphQL API合并为1个

## Second Part

### 查询和变更

#### 1.字段(fields)

简单而言，GraphQL是关于请求对象上的特定字段。例如下面，在本文档中，所有的代码段都是上面是查询，下面是查询所返回的结果。但是出于篇幅的考虑，我们将大部分返回的结果都删掉。

```GraphQL
{	
	#简单查询主角名字
	hero{
		name
	}
}
```

```
{
	"data":{
		"hero":{
			"name":"R2-D2"
		}
	}
}
```

我们可以看到查询和其结果拥有几乎一样的结构，这也是GraphQL最重要的特性，因为这样一来，你就总是能得到你想要的数据。而服务器也准确地知道客户端请求的字段。在上面的例子中我们只是简单的查询了一下主角的名字字段，然后返回了一个字符串类型（String），但是字段也能指代对象类型（Object），这个时候你可以对这个对象的字段进行次级选择。GraphQL查询能够遍历相关对象及其字段，使得客户端可以一次请求查询大量相关数据，而不是像传统REST架构中那样需要多次往返查询。比如我们可以顺便查询主角的朋友的name字段。

```
{
	hero{
		name
		friends{
			name
		}
	}
}
```

#### 2.参数(Agument)

除了遍历对象和字段我们还可以给字段传递参数。

```
{
	human(id:"1000"){
		name
		height
	}
}
```

在类似REST系统中，你只能传递一组简单参数--请求中的query参数和URL段，但是在GraphQL中，每一个字段和嵌套对象都能有自己的一组参数，从而使得GraphQL可以完美替代多次API获取请求。甚至你也可以给标量字段传递参数，用于实现服务端的一次转换，而不用每个客户端分别转换。

```
{
	human(id:"1000"){
		name
		height(unit:FOOT) #此处的unit是一个枚举类型，包括了METER和FOOT
	}
}
```

```
{
    "data": {
        "human": {
        	"name": "Luke Skywalker",
        	"height": 5.6430448
        }
    }
}
```

参数可以是多种不同的类型，比如上面我们使用了String和枚举类型的参数。

#### 3. 别名

即便结果中的字段与查询中的字段能够匹配，但是因为他们并不包含参数，你就没法通过不同参数来查询相同字段。这也是为什么需要别名的原因——这可以让你重命名结果中的字段为任意你想到的名字。

```
{
	#如果这里我们不使用别名而是简单的将直接查询在不同电影中的两个主角名字是查不到的，但是这个可以是一个有别名一个没有，这也是可以查出来的
	empireHero:hero(episode:EMPIRE){
		name
	}
	jediHero:hero(episode:JEDI){
		name
	}
}

{
  "data": {
    "empireHero": {
      "name": "Luke Skywalker"
    },
    "jediHero": {
      "name": "R2-D2"
    }
  }
}

#查询不到的情况，此时提示字段hero产生冲突，因为他们有不同的参数，需要设置不同的别名
{
	hero(episode:EMPIRE){
		name
	}
	hero(episode:JEDI){
		name
	}
}
```

#### 4. 片段(Fragments)

假设我们的app有比较复杂的页面，比如需要将正反派主角及其友军分为两拨。你立马就能想到对应的查询会变得复杂，因为我们需要将一些字段重复至少一次——两方各一次以作比较。

这也就是为何GraphQL包含了称作片段的可复用单元。片段使你能够组织一组字段，然后在需要它们的地方引入。例如下面。

```
{
	leftComparison:hero(episode:EMPIRE){
		#使用片段的方式 ...片段名
		...comparisonFields
	}
	rightComparison:hero(episode:JEDI){
		...comparisonFields
	}
}
fragment comparisonFields on Character{
	name
	appearsIn
	friends{
		name
	}
}
```

片段的概念经常用于将复杂的应用数据需求分割成小块，特别是你要将大量不同片段的UI组件组合成一个初始数据获取的时候。

片段中也是可以访问查询或变更中声明的变量，比如例子中一个是有四条数据一个有三条数据，但是因为使用了 ```(first:$first)```所以只会显示前两条数据。

```
query HeroComparison($first: Int = 2){
	leftComparison:hero(episode:EMPIRE){
		...compaisonFields
	}
	rightComparison:hero(episode:JEDI){
		...comparisonFields
	}
}
fragment comparisonFields on Character{
	name
	friendConnection(first:$first){
		totalCount
		edges{
			node{
				name
			}
		}
	}
}
```

#### 5.操作名称(Operation name)

上面的几种写法都是简写，省略了query关键字和查询名称，但是在生产中是不应该省略的

操作类型可以是query、mutation或subscription，描述你打算做什么类型的操作。操作类型是必需的，除非我们使用query简写语法，在这种情况下我们无法为操作提供名称或者变量定义。

操作名称是你的操作的有意义和明确的名称。它仅在有多个操作的文档中是必须的，但我们鼓励使用它，因为它对于调试和服务器端日志记录非常有用。当在你的网络或是GraphQL服务器的日志中出现问题时。通过名称来从你的代码库中找到一个查询比尝试去破译内容更加容易。

#### 6.变量(Variables)

在很多应用中，字段的参数可能是动态的，比如下拉菜单或者是一个搜索区，或者是一组过滤器。

将这些动态参数直接传进查询字符串并不好，因为此时需要客户端动态地在运行时操作这些查询字符串，再把它序列化成GraphQL专用的格式。其实，GraphQL拥有一级方法将动态值提取到查询之外，然后作为分离的字典传进去。这些动态之即称为变量。

使用变量之前我们要做三件事

     1. 使用$variableName替代查询中的静态值
        2. 声明$variableName为查询接受的变量之一
        3. 将variableName:value通过传输专用（通常是JSON）将其分离到变量字典中

```
query HeroNameAndFriedns($episode:Episode){	#变量定义
	hero(episode:$Episode){ #声明$episode为查询接受的变量之一
		name
		friends{
			name
		}
	}
}

VARIABLES
{
	"episode":"JEDI"
}
```

现在我们可以在我们的客户端代码中简单的传输不同的变量而不是需要构建一个全新的查询，对于标记我们查询中的哪个参数期望是动态的，这是一个较好的实现方式。我们决不能使用用户提供的值来直接字符串插值以构建查询。必须将其分离到变量字典中。

+   变量定义(Variable Definitions)

变量定义其实就是上面例子中的```$episode:Episode```，在这里必须列出所有的变量，前缀必须是$，后跟其类型，在本例中只有一个变量episode。

所有声明的变量都必须是标量、枚举型或者输入对象类型。所以如果想要传递一个复杂对象到一个字段上，你必须知道服务器上其匹配的类型。我们将在后面的章节中了解更多关于输入对象类型的信息

变量第一可以是可选的或者必要的，上例中，Episode后没有!因此其是可选的。但是如果你想传递变量的字段要求非空参数，那变量一定是必要的。这一部分将在Schema中做进一步细述。TODO

+   默认变量(Default variables)

可以通过在查询中的类型定义后面附带默认值的方式，将默认值赋给变量

```
query HeroNameAndFriends($episode:Episode = "JEDI"){
	hero(episode:$episode){
		name
			friends{
				name
			}
	}
}
```

当所有变量都有默认值的时候，可以不传变量直接调用查询。如果任何变量作为变量字典的部分传递，则直接覆盖默认值。

#### 7.指令(Directives)

上面讨论的变量使我们可以避免手动字符串插值构建动态查询。传递带有参数的变量解决了一大堆这样的问题，但是可能在某种情况下我们需要一个方式使用变量动态地改变我们查询的结构。假如有一个UI组件，其有概括视图和详情视图，后者比前者拥有更多的字段。

```
query Hero($episode:Episode,$withFriends:Boolean!){
	hero(episode:$episode){
		name
		friends @include(if: $withFriends){
			name
		}
	}
}
VARIABLES
{
	"episode":"JEDI",
	"withFriends":true
}
```

```
{
  "data": {
    "hero": {
      "name": "R2-D2",
      #当上面的withFriends为false时，则下面的friends就会不显示
      "friends": [
        {
          "name": "Luke Skywalker"
        },
        {
          "name": "Han Solo"
        },
        {
          "name": "Leia Organa"
        }
      ]
    }
  }
}
```

在上面使用了一种称作指令的新特性。一个指令可以附着在片段或者片段包含的字段上，然后以任何服务端期待的方式来改变查询的执行。GraphQL的核心规范包含两个指令，其必须被任何规范兼容的GraphQL服务器实现所支持：

+   ```@include(if:boolean)```仅在参数为true时，包含此字段
+   ```@skip(if:boolean)```如果参数为true，则跳过此字段

指令在你不得不操作通过字符串操作来增减查询的字段时解救你。服务端实现也可以定义新的指令来添加新的特性。

#### 7.变更(Mutations)

GraphQL的大部分讨论集中在数据获取，但是任何完整的数据平台也都需要一个改变服务端数据的方法。

REST中，任何请求都可能最后导致一些服务端副作用，但是约定不适用GET请求来修改数据。GraphQL也是一样，技术上而言，任何查询都可以被实现为导致数据写入。然而，建一个约定来规范任何导致写入的操作都应该显式通过变更来发送。

就如同查询一样，如果任何变更字段返回一个对象类型，你也能请求其嵌套字段。获取一个对象变更后的新状态也是十分有用的。

```
mutation CreateReviewForEpisode($ep:Episode!,$review:ReviewInput!){ #review是一个输入对象类型
	createReview(episode:$ep,review:$review){
		stars
		commentary
	}
}

VARIABLES
{
	"ep":"JEDI",
	"review":{
		"stars":5
		"commentary":"This is a great movie!"
	}
}
```

注意createReview字段如何返回了新建的review的stars和commentary字段。这在变更已有数据时特别有用，例如当一个字段自增的时候，我们可以在一个请求中变更并查询这个字段的新值。

注意这里的review变量并非标量。它是一个输入对象类型，一种特殊的对象类型，可以作为参数传递，在schema中会详述。TODO

+   变更中的多个字段(Multiple fields in mutations)

一个变更也能包含多个字段，类似查询。查询和变更名称之外的一个重要区别是：**查询字段时，是并行执行，而变更字段时，是线性执行，一个接着一个。 **

这意味着如果我们一个请求中发送了两个incrementCredits变更，第一个保证在第二个之前执行，以确保不会出现竞态。

#### 8.内联片段(Inline Fragments)

如果你查询的字段返回的是接口和联合类型，那么你可能需要使用内联片段来取出下层具体类型的数据

```
query HeroForEpisode($ep:Episode){
	hero(episode:$ep) {
		name
		#内联片段，当ep的值为对应的
		... on Droid {
			primaryFunction
		}
		... on Human {
			height
		}
	}
}

VARIABLE
{
	"ep":"JEDI"
}
```

```
{
  "data": {
    "hero": {
      "name": "R2-D2",
      "primaryFunction": "Astromech"
    }
  }
}
```

这个查询中，hero字段返会Character类型，取决于episode参数，它可能是Driod或者Human类型的。在直接选择的情况下，你只能请求Character上存在的字段，比如name。

如果要请求具体类型上的字段，你需要使用一个类型条件内联片段。因为第一个片段标注为```...on Droid```，primaryFunction仅在hero返回的Character为Droid类型时才会执行。同理Human的height字段

+   元字段(Meta fields)

某些情况你并不知道你将从GraphQL服务获得什么类型，这时候你就需要一些方法在客户端来决定如何处理这些数据。GraphQL允许你在查询的任何位置请求```__typename```，一个元字段，以获得那个位置的对象类型名称。

```
{
	search(text:"an"){
		__typename
		... on Human {
			name
		}
		... on Droid {
			name
		}
		... on Starship {
			name
		}
	}
}
```

```
{
  "data": {
    "search": [
      {
        "__typename": "Human",
        "name": "Han Solo"
      },
      {
        "__typename": "Starship",
        "name": "TIE Advanced x1"
      }
    ]
  }
}
```

在上面的查询中，search返回了一个联合类型，它可能是三种选项之一。而如果不用```__typename```字段的情况下，几乎不可能在客户端分辨开这三个不同的类型。

## Third Part

### Schema和类型

#### 1. 类型系统（Type System）

为什么需要schema呢，因为我要知道我可以选择什么字段，服务器会返回哪种对象，这些对象下有哪些字段可用。这样当查询到来时就可以根据schema验证并执行查询。与GraphQL沟通时，会使用GraphQL schema language。

#### 2.对象类型和字段（Object Types and Fields）

在GraphQL schema中基本的组件就是对象类型，这个对象类型显示了可以从服务上获取到什么类型的对象，以及这个对象所拥有的字段。下面站是一个基本的例子

```
type Character{
	name:String!
	appearsIn:[Episode!]
	length(unit:LengthUnit=METER):Float
}
```

解释一下

Character是一个对象类型，它拥有着一些字段，比如name和appearsIn，在任何的GraphQL查询中，当操作Character类型时，只会得到name和appearsIn，不会得到别的字段。

String是内置的标量类型之一——标量类型是解析到单个标量对象，不可以对其进行次级选择，在后面加一个！代表是非空的，查询时总会返回一个值。

```[Episode!]!```表示了一个Episode数组，也是非空的，查询时总会返回一个数组，Episode也是非空的，所以数组中的每个对象都是一个Episode对象。

length字段上有一个参数unit，在GraphQL中，所有的参数必须具名传递。参数可以是必选或可选的，当一个参数是可选的，我们可以定义一个默认值——如果unit参数没有传递，那么就会使用默认值METER。

#### 3.查询和变更类型

schema有两个特殊类型，查询和变更，他俩定义了每一个GraphQL查询的入口

```
schema{
	query:Query
	mutation:Mutation
}
```

加入有这么一个query

```
query {
	hero{
		name
	}
	droid(id:"2000"){
		name
	}
}
```

那就代表这个GraphQL中肯定有一个Query类型，且这个Query有hero和Droid字段.

```
type Query{
	hero(episode:Episode):Character
	droid(id:ID!):Droid
}
```

#### 4.标量类型

GraphQ有五个自带的标量类型，分别是```Int,String,Float,Boolean,ID```，ID标量类型表示一个唯一标识符，通常用以重新获取对象或者作为缓存中的键，ID类型使用和String一样的方式序列化。然而将其定义为ID意味着并不需要人类可读性。

当然也可以通过```scalar variable```来自定义变量，然后就是实现中如何定义将其序列化、反序列化和验证。

#### 5.枚举类型

枚举可以做到以下两点

+   验证这个类型的任何参数是可选值的一个
+   与类型系统沟通，一个字段总是一个有限制集合的其中一个值

```
enum Acter{
	Chandler
	Joey
	Ross
}
```

无论在schema的哪出使用了Acter，都会返回三个值中的一个

#### 6.列表和非空

列表通过将类型包裹在[]之间实现的，非空的实现是在类型后面加!，可以随便嵌套。

```
Acter:[String!]! #表示列表本身不能为空，也不能有任何空值成员

Acter:null  #无效
Acter:[]  #无效
Acter:['a','b','null'] #无效

Acter:['a']  #有效
```

#### 7.接口

接口是一个抽象类型，实现了接口的对象类型必须包含接口中的所有字段，当然对象类型也可以有自己的额外的字段。当你要返回一个对象或者一组对象，特别是一组不同的类型时，接口就显得特别有用

```
interface Character{
	id:ID!
	appearsIn:[Episode]!
}
type Human implements Character{
	id:ID!
	appearsIn:[Episode]!
	totalCredits:Int	#特有字段
}
type Droid implements Character{
	id:ID!
	appearsIn:[Episode]!
	primaryFunction:String #特有字段
}

query HeroForEpisode($ep:Episode){
	hero(episode:$ep){
		name
		primaryFunction #报错，只能访问接口中的字段，hero返回的是接口类型
	}
}
#如果非要查询primaryFunction这种位于特定对象类型上的字段，我们可以使用上述提到的内联片段
query HeroForEpisode($ep:Episode){
	hero(episode:$ep){
		name
		... on Droid{
			primaryFunction //这就可以查到了
		}
	}
}


{
  "ep": "JEDI"
}
```

#### 8.联合类型

```
union SearchResult = Human | Droid |Starship
```

联合类型和接口特别像，但是它并不指定类型之间的任何共同字段，在定义的schema中任何返回一个SearchResout类型的地方都可能得到三种中的一个。注意，联合类型的成员需要是具体的对象类型，不可以使用接口或者其他联合类型来创造一个联合类型

如果你需要查询一个返回 `SearchResult` 联合类型的字段，可以使用条件片段

```
{
  search(text: "an") {
    __typename
    #因为都实现了character接口，所以直接可以Character这个类型中查他们的公共字段
    ... on Character{
      name
      id
    }
    ... on Human {
      height
    }
    ... on Droid {
      primaryFunction
    }
    ... on Starship {
      id
      name   #这里仍需指明，因为Starship没有实现character接口
      length
    }
  }
}

{
  "data": {
    "search": [
      {
        "__typename": "Human",
        "name": "Han Solo",
        "height": 1.8
      },
      {
        "__typename": "Human",
        "name": "Leia Organa",
        "height": 1.5
      },
      {
        "__typename": "Starship",
        "name": "TIE Advanced x1",
        "length": 9.2
      }
    ]
  }
}
```

#### 8.输入类型

传递复杂对象作为参数传给字段，可以使用输入对象，输入对象和常规对象的关键字不同，一个是input一个是type.。

```
input ReviewInput{
	stars:Int!
	commentary:String
}
```

```
mutation CreateReviewForEpisode($ep:Episode!,$review:ReviewInput!){
	createReview(episode:$ep,review:$review){
		stars
		commentary
	}
}
VARIABLES
{
	"ep":"EMPIRE",
	"review":{
		"stars":5,
		"commentary":"This is a great movie!"
	}
}

{
	"data":{
		"createReview":{
			"stars":5,
			"commentary":"This is a great movie!"
		}
	}
}
```



