## Controller schema validate 

### 简介
  
  用于对使用SpringMvc开发的Web后端应用的接入口进行校验,对有以下注解的入口方法进行校验
  
  1. RequestMapping
  2. GetMapping
  3. PostMapping
  4. PutMapping
  5. PatchMapping
  6. DeleteMapping
  
### 快速使用
 
 在Main函数入口的类使用注解@EnableSchemaValidation即可启用
 
 ```java
@EnableSchemaValidation
public class Launch {
	public static void main(String[] args) {
		SpringApplication.run(Launch.class, args);
	}
}
```
 
### 配置
 
 现在暂时有2个可配置属性
 
 `schema.path` : schema文件的基础路径
 
 `schema.basePackage` : 只对该路径下的方法进行校验处理
 
### 项目构建

执行 `gradle sourcesJar` 把项目打包为jar文件

执行 `gradle install` 把项目安装在本地maven仓库内

### 引用方式

#### Gradle

```gradle

compile('tech.ascs.cityworks:controller-schema-validator:1.0.1')

```

#### Maven

```xml

<dependency>
    <groupId>tech.ascs.cityworks</groupId>
    <artifactId>controller-schema-validator</artifactId>
    <version>1.0.1</version>
</dependency>

```