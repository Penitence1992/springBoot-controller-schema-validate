package tech.ascs.cityworks.validate;

import com.networknt.schema.ValidationMessage;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.cityworks.validate.base.RequestValidate;
import tech.ascs.cityworks.validate.base.ValidateMessageConvert;
import tech.ascs.cityworks.validate.exception.ValidateFailException;
import tech.ascs.cityworks.validate.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

/**
 * Created by RenJie on 2017/6/26 0026.
 * Aop类,对标有以下注解的方法进行切入,并且调用校验,校验不通过抛出RuntimeException
 * <ul>
 *     <li>{@link org.springframework.web.bind.annotation.RequestMapping}</li>
 *     <li>{@link org.springframework.web.bind.annotation.GetMapping}</li>
 *     <li>{@link org.springframework.web.bind.annotation.PostMapping}</li>
 *     <li>{@link org.springframework.web.bind.annotation.PutMapping}</li>
 *     <li>{@link org.springframework.web.bind.annotation.PatchMapping}</li>
 *     <li>{@link org.springframework.web.bind.annotation.DeleteMapping}</li>
 * </ul>
 */
//@Aspect
public class ValidateInterceptor  implements MethodInterceptor {

    private final RequestValidate validateComponent;
    private final ValidateMessageConvert convert;
    public ValidateInterceptor(RequestValidate validateComponent, ValidateMessageConvert convert){
        this.validateComponent = validateComponent;
        this.convert = convert;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)){
            HttpServletRequest request = attributes.getRequest();
            if (Objects.nonNull(request)){
                Method method = invocation.getMethod();
                Set<ValidationMessage> result = validateComponent.validate(request,method, invocation.getArguments());
                if(result.size() > 0){
                    throw new ValidateFailException(StringUtils.joinString(convert.convert(result), ","));
                }
            }
        }
        return invocation.proceed();
    }
}
