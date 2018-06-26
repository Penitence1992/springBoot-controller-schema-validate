package tech.ascs.cityworks.validate.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.networknt.schema.ValidationMessage;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import tech.ascs.cityworks.validate.base.SchemaValidateComponent;
import tech.ascs.cityworks.validate.exception.ValidateInitNotSchemaException;
import tech.ascs.cityworks.validate.utils.MappingUrlUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SwaggerRequestHandlerMapBean extends AbstractRequestHandlerMapBean {

    private final static Set<RequestMethod> DEFAULT_METHOD = Sets.newHashSet(RequestMethod.values());

    private Map<String, SchemaValidateComponent> validateMap = new ConcurrentHashMap<>();

    private final Map<String,Map> schemaMap;
    private final HandlerMethod method;
    private final RequestMappingInfo requestMappingInfo;

    /**
     * 构造映射类
     * @param method 请求方法
     * @param requestMappingInfo SpringMvc的url映射信息
     * @param schemaMap 转换后的schemaMap，key为url+_+{method}的方式
     * @throws ValidateInitNotSchemaException 该请求完全不存在schema的时候抛出此异常,继承RuntimeException
     */
    public SwaggerRequestHandlerMapBean(HandlerMethod method, RequestMappingInfo requestMappingInfo, Map<String,Map> schemaMap) throws ValidateInitNotSchemaException {
        super(SwaggerRequestHandlerMapBean.class);
        this.method = method;
        this.requestMappingInfo = requestMappingInfo;
        this.schemaMap = schemaMap;
        initSchemaValidate();
    }

    private void initSchemaValidate() throws ValidateInitNotSchemaException {
        Set<String> urls = requestMappingInfo.getPatternsCondition().getPatterns(); //(1) 获取所有请求
        Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods(); //(2)获取所有请求方法
        if (methods.size() == 0) {
            methods = DEFAULT_METHOD;
        }
        //(3) 建立多对多映射,构建Map的Key和Schema路径
        methods.forEach(requestMethod -> urls.parallelStream().map(MappingUrlUtils::pathVariableConvert).forEach(url -> {
//            String pathUrl = url.replaceAll(REGEX_PATH_PARAM,"_");
            String schemaPath = url + "_" + requestMethod.name().toLowerCase();
            if(!schemaMap.containsKey(schemaPath)){
                if(isDebugEnable){
                    logger.debug("Not have key {} from the swagger file", schemaPath);
                }
                return;
            }
            try {
                //(4)
                SchemaValidateComponent validateComponent = SchemaValidateComponent.buildFromMap(schemaMap.get(schemaPath));
                String key = requestMethod.toString() + ":" + url;
                validateMap.put(key, validateComponent);
                logger.info("Init schema file about request url : {}:{}, with schema : {}", requestMethod, url, schemaMap.get(schemaPath));
            } catch (IOException e) {
                logger.warn("Mission schema file about request url : {}:{}", requestMethod, url);
            }
        }));
        if (validateMap.size() == 0) {
            throw new ValidateInitNotSchemaException(requestMappingInfo + " mapping urls and methods not have schema mapping");
        }
    }

    /**
     * 使用Json-Schema对JsonNode的值进行校验
     * @param requestMethod 请求方法
     * @param url 请求URL
     * @param node Jackson 的JsonNode 对象
     * @return 返回校验结果
     */
    public Set<ValidationMessage> validate(RequestMethod requestMethod, String url, JsonNode node) {
        String key = generateKey(requestMethod,url);
        if(isDebugEnable){
            logger.debug("Do validate with RequestMethod : {}, RequestUrl : {}, ValidateValue : {}",requestMethod,url,node);
        }
        SchemaValidateComponent component = this.validateMap.get(key);
        return takeResult(component, node);
    }

    public boolean checkHasValidateComponent(RequestMethod requestMethod, String url) {
        return validateMap.get(generateKey(requestMethod,url)) != null;
    }

    public HandlerMethod getMethod() {
        return method;
    }


    public RequestMappingInfo getRequestMappingInfo() {
        return requestMappingInfo;
    }
}
