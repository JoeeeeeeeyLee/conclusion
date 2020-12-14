# SpringBoot框架中的model层，dao层，service层，controller层

## 1. model层

+ model层即数据库实体层，也被称为entity层，pojo层
+ 一般数据库一张表对应一个实体类，类属性同表字段一一对应

## 2. dao层

+ dao层即数据持久层，也被称为mapper层
+ dao层的作用为访问数据库，想数据库发送sql语句，完成数据的增删改查任务

## 3. service层

+ service层即业务逻辑层
+ service层的作用为完成功能设计
+ service层调用dao层接口，接收dao层返回的数据，完成项目的基本功能设计
+ service层还可以再分为interface层和impl层

## 4. controller层

+ controller层即控制层
+ conroller层的功能为请求和控制响应
+ controller层负责前后端交互，接受前端请求，调用service层，接收service层返回的数据，最后返回具体的页面和数据到客户端
