# Map Struct

MapStruct是一个用于生成类型安全，高性能和无依赖的bean映射代码的注释处理器

不使用反射，单纯的get、set方法导致他的速度快且不容易出现异常

## 1. WHAT

MapStruct是一个Java注释处理器，用于生成类型安全的Bean映射类

## 2. WHY

在实际开发中我们需要将DAO，DTO和VO之间进行转换，而他们的大部分属性是一样的，只有少部分的不同，通过Map Struct可以让不同实体间的转换变得简单，不需要不停的写setget。

## 3. USE

基于maven，在`pom.xml`文件中添加两个依赖

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
```

`org.mapstruct:mapstruct`包含了一些必要的注解，比如`@Mapping`

`org.mapstruct:mapstruct-processor`注解处理器，根据注解自动生成mapper的实现

### 4. CASE

### 一. 定义一个映射器

+ 基本映射与给映射器添加自定义方法

```java
@Mapper
public interface CarMapper {
    //对于完全一样的属性则可以省略，只需要写名称不一样的属性
    @Mapping(source = "make", target = "manufacturer"),
    @Mapping(source = "numberOfSeats", target = "seatCount")
        CarDto carToCarDto(Car car);

    @Mapping(source = "name", target = "fullName")
        PersonDto personToPersonDto(Person person);

    default PersonDto personToPersonDto(Person person) {
        //hand-written mapping logic
    }
}
```

@Mapper注解使得MapStruct代码生成器在编译期间创建CarMapper接口的实现。当运行程序后，我们可以在target/classes目录下找到相应的实现类

在生成的方法实现中，源类型（例如Car）的所有可读属性将被复制到目标类型中的相应属性中（例如CarDto）。如果属性在目标实体中具有不同的名称，则可以通过@Mapping注解指定其名称。

```java
// GENERATED CODE
public class CarMapperImpl implements CarMapper {

    @Override
    public CarDto carToCarDto(Car car) {
        if ( car == null ) {
            return null;
        }

        CarDto carDto = new CarDto();

        if ( car.getFeatures() != null ) {
            carDto.setFeatures( new ArrayList<String>( car.getFeatures() ) );
        }
        carDto.setManufacturer( car.getMake() );
        carDto.setSeatCount( car.getNumberOfSeats() );
        carDto.setDriver( personToPersonDto( car.getDriver() ) );
        carDto.setPrice( String.valueOf( car.getPrice() ) );
        if ( car.getCategory() != null ) {
            carDto.setCategory( car.getCategory().toString() );
        }

        return carDto;
    }
}
```

这样如果在Person转换PersonDTO的时候有一些mapstruct不能实现的特殊逻辑的转换，可以在这个接口中实现自定义的方法以default方法的形式呈现，MapStruct生成的类实现了carToCarDto()方法。carToCarDto()中生成的代码将在映射 `driver` 属性时调用手动实现的personToPersonDto()方法。

我们也可以将这个mapper注解的接口声明称抽象类，这样的优势是可以定义新的变量。

+ 多个源参数的映射方法

 MapStruct还支持具有多个源参数的映射方法。这很有用，例如为了将多个实体组合成一个数据传输对象。

```java
@Mapper
public interface AddressMapper {

    @Mappings({
        @Mapping(source = "person.description", target = "description"),
        @Mapping(source = "address.houseNo", target = "houseNumber")
    })
    DeliveryAddressDto personAndAddressToDeliveryAddressDto(Person person, Address address);
}
```

如果多个源对象定义具有相同名称的属性，则必须使用@Mapping注解指定从中检索属性的source参数，如示例中的description属性所示。如果不解决这种歧义，将会引发错误。对于在给定源对象中仅存在一次的属性，可以选择指定源参数的名称，因为它可以自动确定。

使用@Mapping注解时，必须指定属性所在的参数。

如果所有源参数都为null，则具有多个源参数的映射方法将返回null。否则，将实例化目标对象，并且将传递所提供参数的所有属性。

+ 嵌套映射

 MapStruct将通过“.”符号处理嵌套映射

```java
@Mappings({
    @Mapping(target = "chartName", source = "chart.name"),
    @Mapping(target = "title", source = "song.title"),
    @Mapping(target = "artistName", source = "song.artist.name"),
    @Mapping(target = "recordedAt", source = "song.artist.label.studio.name"),
    @Mapping(target = "city", source = "song.artist.label.studio.city"),
    @Mapping(target = "position", source = "position")
})
ChartEntry map(Chart chart, Song song, Integer position);
```

注意：参数名称（`chart`, `song`, `position`）是必需的，因为映射中有多个源参数。 如果只有一个源参数，则可以省略参数名称。

MapStruct将对源中的每个嵌套属性执行空检查。

也可以以这种方式映射非java bean源参数（如java.lang.Integer）。

+ 更新现有的Bean实例

在某些情况下，你需要不创建目标类型的新实例，而是更新该类型的现有实例的映射。可以通过为目标对象添加参数并使用@MappingTarget标记此参数来实现此类映射。以下是一个示例：

```java
@Mapper
public interface CarMapper {
    void updateCarFromDto(CarDto carDto, @MappingTarget Car car);
}
```

生成的updateCarFromDto()方法代码将使用给定CarDto对象的属性更新传递的Car实例。可能只有一个参数标记为映射目标。你也可以将方法的返回类型设置为目标参数的类型，而不是void，这将导致生成的实现更新传递的映射目标并返回它。这允许对映射方法进行流畅的调用。

目标bean的集合或Map类型的属性将在更新中被清除，然后使用相应源集合或Map中的值填充这些属性。

### 二.检索映射器

在Java中一般不使用映射器工厂的模式，而是因为使用了Spring框架，更多的使用依赖注入的思想

```java
//这里还有一个imports注解，可以引入一个需要使用的类
@Mapper(componentModel = "spring",imports="class")
public interface CarMapper {
    CarDto carToCarDto(Car car);
}
//使用
@Autowired
private CarMapper mapper;
```

### 三. 数据类型转换

+ 隐式类型转换

比如基本类型和包装器类型，MapStruct会自动转换，不需要我们工作

+ 映射对象转换

比如Car类中包含了一个Person对象（司机）的引用，该对象应该映射到CarDto类引用的PersonDto对象。**这种情况下，需要为引用的对象类型定义一个映射方法**

```java
@Mapper
public interface CarMapper {
    CarDto carToCarDto(Car car);
    PersonDto personToPersonDto(Person person);
}
```

生成的carToCarDto()方法代码将调用personToPersonDto()方法来映射 `driver` 属性，而生成的personToPersonDto()实现则执行了person对象的映射。

在生成映射方法的实现时，MapStruct将对源和目标对象中的每个“属性对”应用以下规则：

1) 如果源和目标属性具有相同的类型，则该值将简单地从源复制到目标。如果属性是集合（例如List），则集合的副本将被设置到目标属性中。

2) 如果源属性和目标属性类型不同，请检查是否存在另一种映射方法，该方法将源属性的类型作为参数类型，并将目标属性的类型作为返回类型。如果存在这样的方法，则将在生成的映射实现中调用它。

3) 如果不存在此类方法，MapStruct将查看是否存在属性的源类型和目标类型的内置转换。如果是这种情况，生成的映射代码将应用此转换。

4) 否则，将在构建时引发错误，指示不可映射的属性。

+ 调用其他映射器

除了在同一映射器类型上定义的方法之外，MapStruct还可以调用其他类中定义的映射方法，无论是MapStruct生成的映射器还是手写映射方法。这对于在几个类中构建映射代码（例如每个应用程序模块使用一个映射器类型）或者如果要提供MapStruct无法生成的自定义映射逻辑时非常有用。

例如Car类可能包含属性manufacturingDate，而相应的DTO属性是String类型。为了映射这个属性，你可以实现一个映射器类，如下所示：

```java
//在Spring下这个类必须是一个Bean
public class DateMapper {

    public String asString(Date date) {
        return date != null ? new SimpleDateFormat( "yyyy-MM-dd" )
            .format( date ) : null;
    }

    public Date asDate(String date) {
        try {
            return date != null ? new SimpleDateFormat( "yyyy-MM-dd" )
                .parse( date ) : null;
        }
        catch ( ParseException e ) {
            throw new RuntimeException( e );
        }
    }
}
```

在`@Mapper`注解中引用DateMapper类

```java
@Mapper(uses=DateMapper.class)
public class CarMapper {
    CarDto carToCarDto(Car car);
}
```

### 四. 映射集合

集合类型（List，Set等）的映射方式和bean类型的映射方式相同，即通过在映射器接口中定义具有所需源和目标类型的映射方法。 MapStruct支持java集合框架中的各种可迭代类型。

```java
@Mapper
public interface CarMapper {
    Set<String> integerSetToStringSet(Set<Integer> integers);
    List<CarDto> carsToCarDtos(List<Car> cars);
    CarDto carToCarDto(Car car);
}
```

### 五. 映射枚举值

MapStruct支持生成将一个Java枚举类型映射到另一个Java枚举类型的方法。

默认情况下，源枚举中的每个常量都映射到目标枚举类型中具有相同名称的常量。如果需要，可以使用@ValueMapping注解将源枚举中的常量映射到具有其他名称的常量。源枚举中的几个常量可以映射到目标类型中的相同常量。

```java
@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper( OrderMapper.class );
    @ValueMappings({
        @ValueMapping(source = "EXTRA", target = "SPECIAL"),
        @ValueMapping(source = "STANDARD", target = "DEFAULT"),
        @ValueMapping(source = "NORMAL", target = "DEFAULT")
    })
    ExternalOrderType orderTypeToExternalOrderType(OrderType orderType);
}
```

### 六. 高级映射选项

+ 默认值和常量

```java
@Mapper(uses = StringListMapper.class)
public interface SourceTargetMapper {
    SourceTargetMapper INSTANCE = Mappers.getMapper( SourceTargetMapper.class );
    @Mappings( {
        @Mapping(target = "stringProperty", source = "stringProp", defaultValue = "undefined"),
        @Mapping(target = "longProperty", source = "longProp", defaultValue = "-1"),
        @Mapping(target = "stringConstant", constant = "Constant Value"),
        @Mapping(target = "integerConstant", constant = "14"),
        @Mapping(target = "longWrapperConstant", constant = "3001"),
        @Mapping(target = "dateConstant", dateFormat = "dd-MM-yyyy", constant = "09-01-2014"),
        @Mapping(target = "stringListConstants", constant = "jack-jill-tom")
    } )
    Target sourceToTarget(Source s);
}
```

如果 `s.getStringProp() == null`，则目标属性stringProperty将设置为“undefined”，而不是应用s.getStringProp()中的值。如果 `s.getLongProperty() == null`，则目标属性longProperty将设置为-1。字符串“常量值”按原样设置为目标属性stringConstant。值“3001”被类型转换为目标属性longWrapperConstant的Long（包装器）类。日期属性还需要日期格式。常量“jack-jill-tom”演示了如何调用手写类StringListMapper以将以短划线分隔的列表映射到`List <String>`。

+ **表达式**

**该示例演示了如何将源属性时间和格式组合到一个目标属性TimeAndFormat中。请注意，指定了完全限定的包名称，因为MapStruct不会处理TimeAndFormat类的导入（除非在SourceTargetMapper中以其他方式显式使用）。 这可以通过在@Mapper注解上定义导入来解决。**

```JAVA
imports org.sample.TimeAndFormat;

@Mapper( imports = TimeAndFormat.class )
public interface SourceTargetMapper {
    @Mapping(target = "timeAndFormat",
         expression = "java( new TimeAndFormat( s.getTime(), s.getFormat() ) )")
    Target sourceToTarget(Source s);
}
```

### 七. 重用映射配置

+ 映射配置继承

可以使用注解`@InheritConfiguration`将方法级配置注解（如@Mapping，@BeanMapping，@IterableMapping等）从一个映射方法继承到类似方法：

```java
@Mapper
public interface CarMapper {

    @Mapping(target = "numberOfSeats", source = "seatCount")
    Car carDtoToCar(CarDto car);

    @InheritConfiguration
    void carDtoIntoCar(CarDto carDto, @MappingTarget Car car);
}
```

上面的示例声明了一个映射方法carToDto()，其配置用于定义如何映射Car类型中的属性numberOfSeats。在现有Car实例上执行映射的更新方法需要相同的配置才能成功映射所有属性。在方法上声明@InheritConfiguration允许MapStruct搜索继承候选以应用从中继承的方法的注解。

如果A的所有类型（源类型和结果类型）可分配给B的相应类型，则一方法A可以从另一方法B继承配置。需要在当前映射器，父类/接口或共享配置界面（如10.3.共享配置中所述）中定义被认为是继承的方法。

如果多个方法用于继承，则必须在注解中指定方法名称：@InheritConfiguration(name =“carDtoToCar”)。

方法可以使用@InheritConfiguration并通过另外应用@Mapping，@BorderMapping等来覆盖或修改配置。

+ 反向映射

 使用注解@InheritInverseConfiguration指示方法应继承相应反向方法的反向配置。

```java
@Mapper
public interface CarMapper {
    @Mapping(source = "numberOfSeats", target = "seatCount")
    CarDto carToDto(Car car);
    @InheritInverseConfiguration
    Car carDtoToCar(CarDto carDto);
}
```

这里carDtoToCar()方法是carToDto()的反向映射方法。请注意，来自carToDto()的任何属性映射也将应用于相应的反向映射方法。它们会自动反转并使用@InheritInverseConfiguration注解复制到方法中。

来自反转方法的特定映射可以（可选地）被映射中的ignore，expression或constant覆盖，例如，像这样：@Mapping(target = "numberOfSeats", ignore=true)。

如果A的结果类型与B的单一源类型相同并且A的单一源类型与B的结果类型相同，则方法A被认为是方法B的反向方法。

需要在当前映射器（父类/接口）中定义考虑用于反向继承的方法。

如果多个方法符合条件，则需要使用name属性指定从中继承配置的方法：@InheritInverseConfiguration(name = "carToDto")。
