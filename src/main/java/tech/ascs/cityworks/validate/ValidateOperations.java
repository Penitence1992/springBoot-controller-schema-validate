package tech.ascs.cityworks.validate;

import com.networknt.schema.ValidationMessage;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.cityworks.validate.exception.ValidateFailException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by RenJie on 2017/6/30 0030.
 */
public class ValidateOperations implements MethodInterceptor {

    private final ValidateComponent validateComponent;

    public ValidateOperations(ValidateComponent validateComponent) {
        this.validateComponent = validateComponent;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        if(invocation instanceof ReflectiveMethodInvocation) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            ReflectiveMethodInvocation joinPoint = (ReflectiveMethodInvocation) invocation;
            Method method = joinPoint.getMethod();
            Set<ValidationMessage> result = validateComponent.validate(request,method,joinPoint.getArguments());
            if(result.size() > 0){
                throw new ValidateFailException("Validate fail msg : " + result.toString());
            }
        }
        return invocation.proceed();
    }
}
