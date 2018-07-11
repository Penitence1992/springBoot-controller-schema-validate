package tech.ascs.cityworks.validate.convert;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import tech.ascs.cityworks.validate.utils.MappingUrlUtils;
import tech.ascs.cityworks.validate.utils.ResourcesUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Swagger格式文档到json schema的转换器
 */
public class ConvertYmlToSchema {

    public static ConvertYmlToSchemaBuilder builder() {
        return new ConvertYmlToSchemaBuilder();
    }

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private final static Logger LOGGER = LoggerFactory.getLogger(ConvertYmlToSchema.class);

    @Getter
    @Setter
    private boolean flatMode = false;

    @Getter
    @Setter
    private String schemaBasePath;

    private Map data; //yaml转换后的完整map对象

    public Map<String, Map> convert() throws IOException {
        Yaml yaml = new Yaml();
        data = yaml.loadAs(ResourcesUtils.readToString(schemaBasePath), Map.class);
        Map<String, Map> paths = (Map) data.get("paths");
        String basePath = Optional.ofNullable(data.get("basePath")).orElse("").toString();
        Map<String, Map> result = new ConcurrentHashMap<>();
        paths.entrySet()
                .parallelStream()
                .forEach(entry -> result.putAll(convertPathMethods(basePath + MappingUrlUtils.pathVariableConvert(entry.getKey()), entry.getValue())));
        return result;
    }

    private Map<String, Map> convertPathMethods(String path, Map methods) {
        Map<String, Map> result = new HashMap<>();
        methods.forEach((method, content) -> result.put(path.replaceAll("/+","/") + "_" + method, convertContentToSchema((Map) content)));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map convertContentToSchema(Map content) {
        if (!content.containsKey("parameters")) {
            return new HashMap();
        }
        List<Map> param = (List<Map>) content.get("parameters");
        Map schema = createBaseObjectSchema(Optional.ofNullable(content.get("operationId")).orElse("default").toString());
        schema.putAll(convertToProperties(param));
        try {
            LOGGER.info("Validate schema : {}", objectMapper.writeValueAsString(schema));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return schema;
    }

    private Map createBaseObjectSchema(String title) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("type", "object");
        return data;
    }

    @SuppressWarnings("unchecked")
    private Map convertToProperties(List<Map> params) {
        Map fillMap = new HashMap();
        Map properties = new HashMap();
        List<FlatModeMap> modeMaps = params.stream().map(param -> parseParam(param)).collect(Collectors.toList());

        //判断是否使用flat模式
        if (modeMaps.parallelStream().anyMatch(FlatModeMap::isFlatMode)) {
            //获取其中一个作为主要平面目标
            FlatModeMap modeMap = modeMaps.parallelStream().filter(FlatModeMap::isFlatMode).findFirst().get();
            Map property = Optional.ofNullable(modeMap.getProperties()).orElse(new HashMap());
            if (!property.containsKey("properties")) {
                property.put("properties", new HashMap<>());
            }
            //把所有的非flat模式的参数压入其中
            modeMaps.stream().filter(flatModeMap -> !flatModeMap.isFlatMode())
                    .forEach(flatModeMap -> ((Map) property.get("properties")).putAll(flatModeMap.getProperties()));
            fillMap.putAll(property);
        } else {
            modeMaps.forEach(flatModeMap -> properties.putAll(flatModeMap.getProperties()));
            fillMap.put("properties", properties);
        }

        if (!fillMap.containsKey("required")) {
            fillMap.put("required", new HashSet<>());
        }
        //处理参数中的required字段,放入到required中,作为必须参数的校验
        properties.forEach((name, property) -> {
            Map p = (Map) property;
            if (p.containsKey("required") && Boolean.valueOf(p.get("required").toString())) {
                ((Set) fillMap.get("required")).add(Optional.ofNullable(p.get("_requiredName")).orElse(name).toString());
            } else if (p.containsKey("$this") && Boolean.valueOf(p.get("$this").toString())) {
                ((Set) fillMap.get("required")).add(name);
            }
        });
        return fillMap;
    }

    private FlatModeMap parseParam(Map param) {
        FlatModeMap rtv;
        boolean innerFlatMode = false;
        String paramName;
        Map property = new HashMap();
        if (param.containsKey("$ref")) {
            String ref = param.get("$ref").toString();
            param = parseRef(ref, data);
        }
        String in = param.get("in").toString();
        paramName = Optional.ofNullable(param.get("name")).orElse("body".equals(in)? "body" : "param" ).toString();
        //如果参数是位于query或者formData的,使用这种方式构造property
        if ("query".equals(in) || "formData".equals(in)) {
            Map content = new HashMap();
            content.putAll(param);
            if (Optional.ofNullable(content.get("type")).orElse("").equals("file")) {
                content.put("type", "string");
            }
            property.put(param.get("name"), content);
        } else if ("body".equals(in)) {
            Map schema = (Map) param.get("schema");
            Map refMap = schema;
            if (refMap.containsKey("$ref")) {
                // 解析#/开头的ref
                refMap = parseRef(schema.get("$ref").toString(), data);
                // key $this放入一个布尔值,表示这个body是否必须
                refMap.put("$this", Optional.ofNullable(param.get("required")).orElse("false").toString());
            }
            //是否使用flat模式进行处理
            if (flatMode) {
                property.putAll(refMap);
            } else {
                property.put(param.get("name"), refMap);
            }
            innerFlatMode = flatMode;
        }
        rtv = new FlatModeMap(property, paramName, innerFlatMode);
        return rtv;
    }

    private Map parseRef(String path, Map all) {
        Map tmp = all;
        if (path.startsWith("#/")) {
            String[] parsePaths = path.substring(2).split("/");
            for (String p : parsePaths) {
                if (tmp.containsKey(p)) {
                    tmp = (Map) tmp.get(p);
                } else {
                    return new HashMap();
                }
            }
        }
        if (tmp.containsKey("xml")) {
            tmp.remove("xml");
        }
        return tmp;
    }


    public static class ConvertYmlToSchemaBuilder {
        private ConvertYmlToSchema convert;

        private ConvertYmlToSchemaBuilder() {
            convert = new ConvertYmlToSchema();
        }

        public ConvertYmlToSchemaBuilder flatMode(boolean enable) {
            convert.setFlatMode(enable);
            return this;
        }

        public ConvertYmlToSchemaBuilder swaggerFilePath(String swaggerFilePath) {
            convert.setSchemaBasePath(swaggerFilePath);
            return this;
        }

        public ConvertYmlToSchema build() {
            return convert;
        }
    }

    @Data
    @AllArgsConstructor
    private class FlatModeMap {
        private Map properties;

        private String name;

        private boolean flatMode = false;

    }
}
