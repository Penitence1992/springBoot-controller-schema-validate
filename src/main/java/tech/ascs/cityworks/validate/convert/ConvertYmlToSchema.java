package tech.ascs.cityworks.validate.convert;


import org.yaml.snakeyaml.Yaml;
import tech.ascs.cityworks.validate.utils.ResourcesUtils;

import java.io.IOException;
import java.util.*;

public class ConvertYmlToSchema {

    public static Map<String,Map> convert(String schemaBasePath) throws IOException {
        Yaml yaml = new Yaml();
        Map data = yaml.loadAs(ResourcesUtils.readToString(schemaBasePath), Map.class);
        Map paths = (Map) data.get("paths");
        String basePath = Optional.ofNullable(data.get("basePath")).orElse("").toString();
        Map<String, Map> result = new HashMap<>();
        paths.forEach((path, methods) -> result.putAll(convertPathMethods(basePath + path.toString(), (Map) methods, data)));
        return result;
    }

    private static Map<String,Map> convertPathMethods (String path, Map methods, Map all){
        Map<String, Map> result = new HashMap<>();
        methods.forEach( (method, content) -> result.put(path + "_" +method, convertContentToSchema((Map) content, all)));
        return result;
    }

    private static  Map convertContentToSchema(Map content, Map all){
        if(!content.containsKey("parameters")){
            return new HashMap();
        }
        List<Map> param = (List<Map>) content.get("parameters");
        Map schema = createBaseObjectSchema(Optional.ofNullable(content.get("operationId")).orElse("default").toString());
        schema.put("properties",convertToProperties(param, all));
        List<String> required = new ArrayList<>();
        param.forEach(p -> {
            if(p.containsKey("$ref")){
                p = parseRef(p.get("$ref").toString(), all);
            }
            if(p.containsKey("required") && Boolean.valueOf(p.get("required").toString())){
                required.add(p.get("name").toString());
            }
        });
        schema.put("required", required);
        return schema;
    }

    private static Map createBaseObjectSchema(String title){
        Map<String,Object> data = new HashMap<>();
        data.put("title", title);
        data.put("type","object");
        return data;
    }

    private static Map convertToProperties(List<Map> params, Map all){
        Map properties = new HashMap();
        params.forEach(param -> properties.putAll(parseParam(param, all)));
//        System.out.println(properties);
        return properties;
    }

    private static Map parseParam(Map param, Map all){
        Map property = new HashMap();
        if(param.containsKey("$ref")){
            String ref = param.get("$ref").toString();
            param = parseRef(ref, all);
        }
        String in = param.get("in").toString();
        if("query".equals(in)){
            Map content = new HashMap();
            content.putAll(param);
            property.put(param.get("name"), content);
        }else if ("body".equals(in)){
            Map schema = (Map) param.get("schema");
            if(schema.containsKey("$ref")){
                property.put(param.get("name"), parseRef(schema.get("$ref").toString(), all));
            }
        }
        return property;
    }

    private static Map parseRef(String path, Map all){
        Map tmp = all;
        if(path.startsWith("#/")){
            String[] parsePaths = path.substring(2).split("/");
            for (String p : parsePaths){
                if(tmp.containsKey(p)){
                    tmp = (Map) tmp.get(p);
                }else{
                    return new HashMap();
                }
            }
        }
        if(tmp.containsKey("xml")){
            tmp.remove("xml");
        }
        return tmp;
    }
}
