# Tk.Mybatis中的方法解析

1. `BaseSelectMapper`

```java
//根据传入的实体属性查询，如果实体的属性未设置，那么为null，查询条件为=，只能有一个返回值，多个则报错
T selectOne(T record);
//根据传入的实体属性查询，查询所有符合条件的实体
List<T> select(T record);
//查询所有实体
List<T> selectAll();
//根据传入的实体属性查询，查询符合条件的实体的数量
int selectCount(T record);
//根据主键进行查询，如果是联合主键，这种情况下传实体类
T selectByPrimaryKey(Object key);
//根据主键进行查询是否存在，参数必须包含完整主键
boolean existsWithPrimaryKey(Object key);


//根据传入的属性插入实体，如果属性未设置则为null，不会使用数据库默认值，成功返回1，不成功返回0
int insert(T record);
//根据传入的属性插入实体，如果属性未设置，会使用数据库默认值，成功返回1，不成功返回0
int insertSelective(T record);


//根据主键更新实体全部字段，null值会被更新
int updateByPrimaryKey(T record);
//根据主键更新属性不为null的值
int updateByPrimaryKeySelective(T record);


//根据实体属性作为条件进行删除，查询条件使用等号
int delete(T record);
//根据主键字段进行删除，方法参数必须包含完整的主键属性
int deleteByPrimaryKey(Object key);
```

2.`ExampleMapper`

```java
//这个Example在这里是一个过滤条件，只不过这个过滤条件比较特殊，可以做到以下几点，查询、动态SQL、排序、去重、设置查询列
1.查询
Example example=new Example(Country.class);
example.setForUpdate(true);
example.createCriteria().andGreatThan("id",100).andLessThan("id",151);
example.or().andLessThan("id",41);
List<Country> countries = mapper.selectByExample(example);

2.动态SQL
Example example = new Example(Country.class);
Example.Criteria criteria = example.createCriteria();
if(query.getCountryname() != null){
    criteria.andLike("countryname", query.getCountryname() + "%");
}
if(query.getId() != null){
    criteria.andGreaterThan("id", query.getId());
}
List<Country> countries = mapper.selectByExample(example);

3.排序
Example example = new Example(Country.class);
example.orderBy("id").desc().orderBy("countryname").orderBy("countrycode").asc();
List<Country> countries = mapper.selectByExample(example);

4.去重
CountryExample example = new CountryExample();
//设置 distinct
example.setDistinct(true);
example.createCriteria().andCountrynameLike("A%");
example.or().andIdGreaterThan(100);
List<Country> countries = mapper.selectByExample(example);

5.设置查询列
Example example = new Example(Country.class);
example.selectProperties("id", "countryname");
List<Country> countries = mapper.selectByExample(example);

具体可以查看Example源码
//example可以使用builder方式进行构建
Example example = Example.builder(Country.class)
        .select("countryname")
        .where(Sqls.custom().andGreaterThan("id", 100))
        .orderByAsc("countrycode")
        .forUpdate()
        .build();
List<Country> countries = mapper.selectByExample(example);
```

```java
//这下面的前四个都是和上面的对应的
List<T> selectByExample(Object example);
T selectOneByExample(Object example);
int selectCountByExample(Object example);
int deleteByExample(Object example);

//根据Example条件更新实体record包含的全部属性，null值会被更新
int updateByExample(@Param("record") T record, @Param("example") Object example);
//根据Example条件更新实体record包含的不是null的属性值
int updateByExampleSelective(@Param("record") T record, @Param("example") Object example);
```

3.`RowBoundsMapper`

```java
//根据example条件和RowBounds进行分页查询
List<T> selectByExampleAndRowBounds(Object example, RowBounds rowBounds);
//根据实体属性和RowBounds进行分页查询
List<T> selectByRowBounds(T record, RowBounds rowBounds);
```
