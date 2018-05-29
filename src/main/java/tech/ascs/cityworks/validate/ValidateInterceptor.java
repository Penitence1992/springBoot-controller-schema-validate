package tech.ascs.cityworks.validate;

import com.networknt.schema.ValidationMessage;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import tech.ascs.cityworks.validate.base.RequestValidate;
import tech.ascs.cityworks.validate.base.ValidateHttpMethodInterceptor;
import tech.ascs.cityworks.validate.base.ValidateMessageConvert;
import tech.ascs.cityworks.validate.exception.ValidateFailException;
import tech.ascs.cityworks.validate.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;


/**
 * Created by RenJie on 2017/6/26 0026.
 * Aop类,对标有以下注解的方法进行切入,并且调用校验,校验不通过抛出RuntimeException
 * <ul>
 * <li>{@link org.springframework.web.bind.annotation.RequestMapping}</li>
 * <li>{@link org.springframework.web.bind.annotation.GetMapping}</li>
 * <li>{@link org.springframework.web.bind.annotation.PostMapping}</li>
 * <li>{@link org.springframework.web.bind.annotation.PutMapping}</li>
 * <li>{@link org.springframework.web.bind.annotation.PatchMapping}</li>
 * <li>{@link org.springframework.web.bind.annotation.DeleteMapping}</li>
 * </ul>
 */
//@Aspect
public class ValidateInterceptor implements ValidateHttpMethodInterceptor {

    private final RequestValidate validateComponent;
    private final ValidateMessageConvert convert;
    private ThreadLocal<ServerWebExchange> local = new ThreadLocal<>();

    public ValidateInterceptor(RequestValidate validateComponent, ValidateMessageConvert convert) {
        this.validateComponent = validateComponent;
        this.convert = convert;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (Objects.nonNull(local.get())) {
            ServerWebExchange exchange = local.get();
            Method method = invocation.getMethod();
            Set<ValidationMessage> result = validateComponent.validate(exchange, method, invocation.getArguments());
            if (result.size() > 0) {
                throw new ValidateFailException(StringUtils.joinString(convert.convert(result), ","));
            }
        }
        return invocation.proceed();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        local.set(exchange);
        return chain.filter(exchange);
    }
}
