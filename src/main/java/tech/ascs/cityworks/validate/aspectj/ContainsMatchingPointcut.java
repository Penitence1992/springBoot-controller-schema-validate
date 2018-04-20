package tech.ascs.cityworks.validate.aspectj;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 一个根据类名使用Contains方法来判断是否为需要切入的类
 *
 * @author penitence.renjie@gmail.com
 * @date 2018-04-20 11:01:40
 */
public class ContainsMatchingPointcut extends DynamicMethodMatcherPointcut implements ClassFilter {

    private final String patten;
    private final Set<Class<?>> methodAnnotations;

    public ContainsMatchingPointcut(String patten, Set<Class<?>> methodAnnotations) {
        this.patten = patten;
        this.methodAnnotations = methodAnnotations;
    }

    @Override
    public ClassFilter getClassFilter() {
        return this;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return !ClassUtils.isCglibProxyClass(clazz) //判断是否为代理类
                && Objects.nonNull(clazz.getPackage()) //获取包相关类的时候不能为null
                && clazz.getPackage().getName().contains(patten);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        return method.toGenericString().contains(patten)  //是否为当前指定类里面的方法,而不是继承的方法
                && Modifier.isPublic(method.getModifiers()) //判断是否为共有的方法
                && !Modifier.isStatic(method.getModifiers()) //需要该方法不为static
                && Stream.of(method.getAnnotations()).map(Annotation::annotationType).anyMatch(methodAnnotations::contains);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return matches(method, targetClass, (Object)null);
    }
}
