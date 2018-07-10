package tech.ascs.cityworks.validate.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

/**
 * Created by RenJie on 2017/6/26 0026.
 * Schema校验器,实现了SchemaValidate接口,使用了com.networknt:json-schema-validator 的包
 */
public class SchemaValidateComponent implements SchemaValidate {

    private static JsonSchemaFactory JSON_SCHEMA_FACTORY = JsonSchemaFactory.getInstance();

    private final static Logger LOGGER = LoggerFactory.getLogger(SchemaValidateComponent.class);

    private final JsonSchema jsonSchema;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 构造SchemaValidateComponent 类
     * @param schemaPath schema文件的路径,基于classpath:开头的文件会去编译后的class路径读取,或者使用外部文件加载
     * @throws IOException schema文件不存在或者schema文件格式错误的时候抛出异常
     */
    private SchemaValidateComponent(String schemaPath) throws IOException {
        LOGGER.debug("Get schema file in path : {}",schemaPath);
        if(schemaPath.startsWith("classpath:")){
            schemaPath = schemaPath.substring(10);
            Resource resource = new ClassPathResource(schemaPath);
            String schema = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
            jsonSchema = JSON_SCHEMA_FACTORY.getSchema(schema);
        }else{
            InputStream inputStream = new FileInputStream(schemaPath);
            jsonSchema = JSON_SCHEMA_FACTORY.getSchema(inputStream);
        }
    }

    private SchemaValidateComponent(Map schema) throws IOException {
        jsonSchema = JSON_SCHEMA_FACTORY.getSchema(mapper.writeValueAsString(schema));
    }

    /**
     * 静态方法构造类
     */
    public static SchemaValidateComponent build(String schemaPath) throws IOException {
        return new SchemaValidateComponent(schemaPath);
    }

    public static SchemaValidateComponent buildFromMap(Map schema) throws IOException {
        return new SchemaValidateComponent(schema);
    }

    public Set<ValidationMessage> doValidate(JsonNode jsonNode){
        return jsonSchema.validate(jsonNode);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
