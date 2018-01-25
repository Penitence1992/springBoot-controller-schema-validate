package tech.ascs.cityworks.validate.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.networknt.schema.ValidationMessage;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import tech.ascs.cityworks.validate.base.SchemaValidateComponent;
import tech.ascs.cityworks.validate.exception.ValidateInitNotSchemaException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by RenJie on 2017/6/27 0027.
 * 请求处理映射Bean类,将请求路径,方法,和Schema校验连接起来
 */
public class JsonSchemaRequestHandlerMapBean extends AbstractRequestHandlerMapBean {


    private final static String REGEX_PATH_PARAM = "[{}]+";
    private final static String SEPARATOR_CHAR = "/";

    private final static Set<RequestMethod> DEFAULT_METHOD = Sets.newHashSet(RequestMethod.values());

    private Map<String, SchemaValidateComponent> validateMap = new HashMap<>();

    private final String schemaBasePath;
    private final HandlerMethod method;
    private final RequestMappingInfo requestMappingInfo;


    /**
     * 构造映射类
     * @param method 请求方法
     * @param requestMappingInfo SpringMvc的url映射信息
     * @param schemaBasePath schema文件的基础路径
     * @throws ValidateInitNotSchemaException 该请求完全不存在schema的时候抛出此异常,继承RuntimeException
     */
    public JsonSchemaRequestHandlerMapBean(HandlerMethod method, RequestMappingInfo requestMappingInfo, String schemaBasePath) throws ValidateInitNotSchemaException {
        super(JsonSchemaRequestHandlerMapBean.class);
        this.method = method;
        this.requestMappingInfo = requestMappingInfo;
        this.schemaBasePath = parseBasePath(schemaBasePath);
        initSchemaValidate();
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


    /**
     * 1. 获取所有url请求
     * 2. 获取所有请求方法
     * 3. 建立多对多映射,构建Map的Key和Schema路径
     * 4. 初始化SchemaValidateComponent类,并装入Map
     * @throws ValidateInitNotSchemaException 当Map的大小为0的时候抛出此异常
     */
    private void initSchemaValidate() throws ValidateInitNotSchemaException {
        Set<String> urls = requestMappingInfo.getPatternsCondition().getPatterns(); //(1) 获取所有请求
        Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods(); //(2)获取所有请求方法
        if (methods.size() == 0) {
            methods = DEFAULT_METHOD;
        }
        //(3) 建立多对多映射,构建Map的Key和Schema路径
        methods.forEach(requestMethod -> urls.forEach(url -> {
            String pathUrl = url.replaceAll(REGEX_PATH_PARAM,"_");
            String schemaPath = schemaBasePath + pathUrl + SEPARATOR_CHAR + requestMethod.name() + ".json";
            if(isDebugEnable){
                logger.debug("Load {}.json file in path {}",requestMethod.name(),schemaPath);
            }
            try {
                //(4)
                SchemaValidateComponent validateComponent = SchemaValidateComponent.build(schemaPath);
                String key = requestMethod.toString() + ":" + url;
                validateMap.put(key, validateComponent);
                logger.info("Init schema file about request url : {}:{}, with path : {}", requestMethod, url, schemaPath);
            } catch (IOException e) {
                logger.warn("Mission schema file about request url : {}:{}, with path : {}", requestMethod, url, schemaPath);
            }
        }));
        if (validateMap.size() == 0) {
            throw new ValidateInitNotSchemaException(requestMappingInfo + " mapping urls and methods not have schema mapping");
        }
    }

    private String parseBasePath(String basePath) {
        if (basePath.endsWith("/")) {
            return basePath.substring(0, basePath.length() - 1);
        } else {
            return basePath;
        }
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
