package tech.ascs.cityworks.validate;

import com.networknt.schema.ValidationMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.ascs.cityworks.validate.exception.ValidateFailException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
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
@Aspect
public class ValidateInterceptor  {

    private final ValidateComponent validateComponent;

    public ValidateInterceptor(ValidateComponent validateComponent){
        this.validateComponent = validateComponent;
    }

    @Around(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)",argNames = "proceedingJoinPoint")
    public Object aroundAOP(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        if(proceedingJoinPoint instanceof MethodInvocationProceedingJoinPoint) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            MethodInvocationProceedingJoinPoint joinPoint = (MethodInvocationProceedingJoinPoint) proceedingJoinPoint;
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Set<ValidationMessage> result = validateComponent.validate(request,method,proceedingJoinPoint.getArgs());
            if(result.size() > 0){
                throw new ValidateFailException("Validate fail msg : " + result.toString());
            }
        }
        return proceedingJoinPoint.proceed();
    }

}
