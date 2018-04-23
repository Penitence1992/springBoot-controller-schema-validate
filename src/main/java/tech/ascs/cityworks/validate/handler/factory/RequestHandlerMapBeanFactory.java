package tech.ascs.cityworks.validate.handler.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import tech.ascs.cityworks.validate.base.RequestHandlerValidate;
import tech.ascs.cityworks.validate.config.SchemaValidateProperties;
import tech.ascs.cityworks.validate.convert.ConvertYmlToSchema;
import tech.ascs.cityworks.validate.handler.JsonSchemaRequestHandlerMapBean;
import tech.ascs.cityworks.validate.handler.SwaggerRequestHandlerMapBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 任杰 2018年01月25日 15:21:35
 * RequestHandlerValidate实例的工程，通过工程去适配是使用Schema文件还是swagger的yml文件进行初始化
 */
public class RequestHandlerMapBeanFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestHandlerMapBeanFactory.class);
    private final static Map<String,Object> cacheMap = new HashMap<>();

    /**
     * 获取一个{@link tech.ascs.cityworks.validate.base.RequestHandlerValidate}的实例，根据传入的schemaBasePath去区分是swagger文件还是schema文件，已yml结尾的为swagger文件模式
     * @param method 控制器的方法
     * @param requestMappingInfo 关联的路径映射信息
     * @param properties schema校验器的配置属性
     * @return 返回一个RequestHandlerValidate实例
     */
    public static RequestHandlerValidate newInstance(HandlerMethod method, RequestMappingInfo requestMappingInfo, SchemaValidateProperties properties){
        String schemaBasePath = properties.getPath();
        if(schemaBasePath.endsWith("yml")){
            try {
                if(!cacheMap.containsKey(schemaBasePath)){
                    cacheMap.put(
                            schemaBasePath,
                            ConvertYmlToSchema.builder()
                                    .flatMode(properties.isFlatMode())
                                    .swaggerFilePath(schemaBasePath)
                                    .build()
                                    .convert()
                    );
                }
                Map<String, Map> schemaMap = (Map<String, Map>) cacheMap.get(schemaBasePath);
                return new SwaggerRequestHandlerMapBean(method, requestMappingInfo, schemaMap);
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
                return null;
            }
        }else{
            return new JsonSchemaRequestHandlerMapBean(method, requestMappingInfo, schemaBasePath);
        }
    }
}
