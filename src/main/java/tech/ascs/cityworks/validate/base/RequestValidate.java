package tech.ascs.cityworks.validate.base;


import com.networknt.schema.ValidationMessage;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Method;
import java.util.Set;

public interface RequestValidate {

    Set<ValidationMessage> validate(ServerWebExchange exchange, Method method, Object[] args);
}
