package tech.ascs.cityworks.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tech.ascs.cityworks.validate.base.RequestHandlerValidate;
import tech.ascs.cityworks.validate.base.RequestQueryBean;
import tech.ascs.cityworks.validate.exception.ValidateInitNotSchemaException;
import tech.ascs.cityworks.validate.handler.factory.RequestHandlerMapBeanFactory;
import tech.ascs.cityworks.validate.utils.ReflectTools;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by RenJie on 2017/6/26 0026.
 * schema校验组件用于初始化,以及对请求进行校验
 */
public class ValidateComponent {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Method, RequestHandlerValidate> cacheMapping = new HashMap<>();

    private final ObjectMapper mapper;

    public ValidateComponent(String basePackage,
                             String schemaBasePath,
                             ObjectMapper mapper,
                             ApplicationContext applicationContext) {
        logger.info("Init tech.ascs.cityworks.validate info by base package : {}", basePackage);
        this.mapper = mapper;

        RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        mapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            if (StringUtils.isEmpty(basePackage) || handlerMethod.getShortLogMessage().contains(basePackage)) {
                try {
                    cacheMapping.put(handlerMethod.getMethod(), RequestHandlerMapBeanFactory.newInstance(handlerMethod, requestMappingInfo, schemaBasePath));
                } catch (ValidateInitNotSchemaException exception) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Init schema error : {}", exception.getMessage());
                    }
                }
            }
        });
        logger.info("Cache mapping has key size : {}", cacheMapping.keySet().size());
    }

    public Set<ValidationMessage> validate(HttpServletRequest request, Method method, Object[] args) {
        Set<ValidationMessage> validateResult = new HashSet<>();
        Optional.ofNullable(cacheMapping.get(method))
                .ifPresent(bean -> {
                    String url = getRequestSourceUrl(bean, request);
                    RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());
                    if (!StringUtils.isEmpty(url) &&
                            bean.checkHasValidateComponent(requestMethod, url)) {
                        Map<String, Object> map = buildValidateMap(bean, args);
                        validateResult.addAll(bean.validate(requestMethod, url, convertMapToJsonNode(map)));
                    }
                });
        if(logger.isDebugEnabled()){
            logger.debug("Validate Result : {}", validateResult);
        }
        return validateResult;
    }

    private Object tryParseStringToMap(String json) {
        Object returnData;
        try {
            returnData = mapper.readValue(json, Map.class);
        } catch (IOException e) {
            logger.warn("Try parse json string faild");
            returnData = json;
        }
        return returnData;
    }

    private String getRequestSourceUrl(RequestHandlerValidate bean, HttpServletRequest request) {
        Set<String> urls = bean.getRequestMappingInfo().getPatternsCondition().getMatchingCondition(request).getPatterns();
        logger.debug("Found matching url count : {}", urls.size());
        if (urls.size() > 1) {
            logger.warn("Found greater than 1 count url, get the first one");
            return urls.iterator().next();
        } else if (urls.size() < 1) {
            logger.error("Matching error and found 0 count url, return null");
            return null;
        }
        return urls.iterator().next();
    }

    private JsonNode convertMapToJsonNode(Map<String, Object> map) {
        try {
            String json = mapper.writeValueAsString(map);
            return mapper.readTree(json);
        } catch (IOException e) {
            logger.error("Convert map to json node error");
            return mapper.createObjectNode();
        }
    }

    private Map<String, Object> buildValidateMap(RequestHandlerValidate bean, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        Stream.of(bean.getMethod().getMethodParameters()).forEach(methodParameter ->{
                    Object data = args[methodParameter.getParameterIndex()];
                    if (methodParameter.getParameterAnnotation(RequestBody.class) != null) {
                        map.put(methodParameter.getParameterName(), parseRequestBody(data));
                    }else{
                        map.putAll(parseRequestParam(methodParameter, data));
                    }
                }
        );
        return map;
    }

    private Map<String, Object> parseRequestParam(MethodParameter methodParameter, Object data){
        Class parameterType = methodParameter.getParameterType();
        Map<String, Object> returnData = new HashMap<>();
        if(parameterType.getAnnotation(RequestQueryBean.class) != null){
            returnData.putAll(parseObjectToMap(data));
        }else if (parameterType.isAssignableFrom(Map.class)){
            returnData.putAll((Map<String, Object>) data);
        }else{
            returnData.put(methodParameter.getParameterName(), data);
        }
        return returnData;
    }

    private Object parseRequestBody(Object data){
        Object returnData;
        if (data instanceof String) {
            returnData = tryParseStringToMap(data.toString());
        } else {
            returnData = data;
        }
        return returnData;
    }

    private Map<String, Object> parseObjectToMap(Object data){
        Map<String,Object> result = new HashMap<>();
        Stream.of(data.getClass().getDeclaredMethods())
                .map(Method::getName)
                .filter(name -> name.startsWith("is") || name.startsWith("get"))
                .forEach(name -> Optional.ofNullable(ReflectTools.invokeMethod(data, name)).ifPresent(obj -> {
                    try {
                        result.put(ReflectTools.parseMethodNameToFiledName(name), obj);
                    } catch (NoSuchMethodException ignored) {
                    }
                }));
        return result;
    }
}
