package tech.ascs.cityworks.validate.base;

import com.networknt.schema.ValidationMessage;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Set;

public interface RequestValidate {

    Set<ValidationMessage> validate(HttpServletRequest request, Method method, Object[] args);
}
