package tech.ascs.cityworks.validate.aspectj;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 这个类使用存在异常暂时未解决, 使用这个类去注册切面的时候,会报循环依赖
 *
 * @author penitence.renjie@gmail.com
 * @date 2018-04-20 11:00:38
 */
public class AspectJExpressionsPointcut extends DynamicMethodMatcherPointcut implements ClassFilter {

    private Set<AspectJExpressionPointcut> pointcuts = new CopyOnWriteArraySet<>();

    public AspectJExpressionsPointcut() {

    }

    public void setExpression(String... expressions){
        pointcuts.addAll(
                Stream.of(expressions)
                        .parallel()
                        .map(this::buildExpressionPointcut)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        return pointcuts.parallelStream()
                .anyMatch(pointcut -> pointcut.matches(method, targetClass, args));
    }

    private AspectJExpressionPointcut buildExpressionPointcut(String expression) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(expression);
        return pointcut;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return pointcuts.parallelStream()
                .anyMatch(pointcut -> pointcut.matches(clazz));
    }
}
