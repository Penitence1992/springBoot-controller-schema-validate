package tech.ascs.cityworks.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tech.ascs.cityworks.validate.base.RequestHandlerValidate;
import tech.ascs.cityworks.validate.base.RequestValidate;
import tech.ascs.cityworks.validate.config.SchemaValidateProperties;
import tech.ascs.cityworks.validate.exception.ValidateInitNotSchemaException;
import tech.ascs.cityworks.validate.handler.factory.RequestHandlerMapBeanFactory;
import tech.ascs.cityworks.validate.utils.MappingUrlUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by RenJie on 2017/6/26 0026.
 * schema校验组件用于初始化,以及对请求进行校验
 */
public class ValidateComponent implements RequestValidate {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Method, RequestHandlerValidate> cacheMapping = new HashMap<>();

    private final ObjectMapper mapper;

    private final boolean FLAT_MODE;

    private final Map<String, String> urlMapping = new ConcurrentHashMap<>();

    public ValidateComponent(SchemaValidateProperties properties,
                             ObjectMapper mapper,
                             ApplicationContext applicationContext) {
        logger.info("Init tech.ascs.cityworks.validate info by base package : {}", properties.getBasePackage());
        this.mapper = mapper;
        this.FLAT_MODE = properties.isFlatMode();
        RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        mapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            if (StringUtils.isEmpty(properties.getBasePackage()) || handlerMethod.getShortLogMessage().contains(properties.getBasePackage())) {
                try {
                    cacheMapping.put(handlerMethod.getMethod(), RequestHandlerMapBeanFactory.newInstance(handlerMethod, requestMappingInfo, properties));
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
                        Object validateData = buildValidateMap(bean, args);
                        validateResult.addAll(bean.validate(requestMethod, url, convertMapToJsonNode(validateData)));
                    }
                });
        if (logger.isDebugEnabled()) {
            logger.debug("Validate Result : {}", validateResult);
        }
        return validateResult;
    }

    private Map tryParseStringToMap(String json) throws IOException {
        Map returnData;
        returnData = mapper.readValue(json, Map.class);
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
        String url = urls.iterator().next();
        if (!urlMapping.containsKey(url)) {
            urlMapping.put(url, MappingUrlUtils.pathVariableConvert(url));
        }
        return urlMapping.get(url);
    }

    private JsonNode convertMapToJsonNode(Object map) {
        try {
            String json = mapper.writeValueAsString(map);
            return mapper.readTree(json);
        } catch (IOException e) {
            logger.error("Convert map to json node error");
            return mapper.createObjectNode();
        }
    }

    private Object buildValidateMap(RequestHandlerValidate bean, Object[] args) {
        if (FLAT_MODE) {
            return buildFlatValidateData(bean, args);
        } else {
            return buildValidateData(bean, args);
        }
    }

    private Object buildFlatValidateData(RequestHandlerValidate bean, Object[] args) {
        //检查是否存在array类型
        Optional<MethodParameter> optParame = Stream.of(bean.getMethod().getMethodParameters()).filter(arg -> arg.hasParameterAnnotation(RequestBody.class))
                .filter(arg -> args[arg.getParameterIndex()] instanceof Collection || checkIsStringAndStartWith(args[arg.getParameterIndex()], "["))
                .findAny();
        if (optParame.isPresent()) {
            return parseCollectionRequestBody(args[optParame.get().getParameterIndex()]);
        } else {
            return buildFlatValidateMap(bean, args);
        }
    }

    private Map<String, Object> buildFlatValidateMap(RequestHandlerValidate bean, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        Stream.of(bean.getMethod().getMethodParameters()).forEach(methodParameter -> {
                    Object data = args[methodParameter.getParameterIndex()];
                    if (data == null) return;
                    if (ignore(data)) return;
                    if (methodParameter.getParameterAnnotation(RequestBody.class) != null) {
                        map.putAll(parseRequestBody(data));
                    } else if (methodParameter.getParameterAnnotation(PathVariable.class) != null) {
                        map.put(findPathVariableName(methodParameter), data);
                    } else {
                        map.putAll(parseRequestParam(methodParameter, data));
                    }
                }
        );

        return map;
    }

    private Map<String, Object> buildValidateData(RequestHandlerValidate bean, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        Stream.of(bean.getMethod().getMethodParameters()).forEach(methodParameter -> {
                    Object data = args[methodParameter.getParameterIndex()];
                    if (data == null) return;
                    if (ignore(data)) return;
                    if (methodParameter.getParameterAnnotation(RequestBody.class) != null) {
                        map.put(methodParameter.getParameterName(), selectMapOrString(data));
                    } else if (methodParameter.getParameterAnnotation(PathVariable.class) != null) {
                        map.put(findPathVariableName(methodParameter), data);
                    } else {
                        map.putAll(parseRequestParam(methodParameter, data));
                    }
                }
        );

        return map;
    }

    private Map<String, Object> parseRequestParam(MethodParameter methodParameter, Object data) {
        Class parameterType = methodParameter.getParameterType();
        Map<String, Object> returnData = new HashMap<>();
        if (parameterType.isAssignableFrom(MultipartFile.class)) {
            MultipartFile file = (MultipartFile) data;
            if (Objects.nonNull(file) && !file.isEmpty()) {
                returnData.put(methodParameter.getParameterName(), file.getOriginalFilename());
            }
        } else {
            returnData.putAll(parseObjectToMap(methodParameter.getParameterName(), data));
        }
        return returnData;
    }

    private Object selectMapOrString(Object data) {
        Map rtv = parseRequestBody(data);
        if (rtv == null) {
            return data.toString();
        }
        return rtv;
    }

    private Map parseRequestBody(Object data) {
        Map returnData;
        if (data instanceof String) {
            try {
                returnData = tryParseStringToMap(data.toString());
            } catch (IOException e) {
                logger.info("this is not json data");
                return null;
            }
        } else {
            Set<Map.Entry> entries = mapper.convertValue(data, Map.class).entrySet();
            returnData = entries.stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return returnData;
    }

    private Collection parseCollectionRequestBody(Object data) {
        if (data instanceof String) {
            try {
                return mapper.readValue(data.toString(), Collection.class);
            } catch (IOException e) {
                logger.info("this is not json data");
                return null;
            }
        } else {
            return (Collection) data;
        }
    }

    private Map parseObjectToMap(String parameterName, Object data) {
        try {
            Set<Map.Entry> entries = mapper.convertValue(data, HashMap.class).entrySet();
            return entries.parallelStream().filter(entry -> Objects.nonNull(entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put(parameterName, data);
            return result;
        }

    }

    private boolean ignore(Object data) {
        return ServletRequest.class.isAssignableFrom(data.getClass()) ||
                ServletResponse.class.isAssignableFrom(data.getClass()) ||
                HttpSession.class.isAssignableFrom(data.getClass());
    }

    private String findPathVariableName(MethodParameter parameter) {
        return Optional.ofNullable(parameter.getParameterName())
                .orElse(findPathVariableAnnName(parameter.getParameterAnnotation(PathVariable.class)));
    }

    private String findPathVariableAnnName(PathVariable pathVariable) {
        if (StringUtils.isEmpty(pathVariable.name())) {
            return pathVariable.value();
        } else {
            return pathVariable.name();
        }
    }

    private boolean checkIsStringAndStartWith(Object data, String str) {
        Assert.notNull(data, "Check data not be null");
        return data instanceof String && data.toString().startsWith(str);
    }
}
