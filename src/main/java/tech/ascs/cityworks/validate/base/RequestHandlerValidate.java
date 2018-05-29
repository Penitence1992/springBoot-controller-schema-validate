package tech.ascs.cityworks.validate.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;

import java.util.Set;

public interface RequestHandlerValidate {

    Set<ValidationMessage> validate(RequestMethod requestMethod, String url, JsonNode node);

    boolean checkHasValidateComponent(RequestMethod requestMethod, String url);

    RequestMappingInfo getRequestMappingInfo();

    HandlerMethod getMethod();
}
