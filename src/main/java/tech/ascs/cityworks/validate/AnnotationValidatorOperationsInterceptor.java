package tech.ascs.cityworks.validate;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RenJie on 2017/6/30 0030.
 */
public class AnnotationValidatorOperationsInterceptor implements IntroductionInterceptor, BeanFactoryAware {


    private ValidateComponent validateComponent;

    private final Map<Method, MethodInterceptor> delegates = new HashMap<>();

    private BeanFactory beanFactory;

    public AnnotationValidatorOperationsInterceptor(ValidateComponent validateComponent) {
        this.validateComponent = validateComponent;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MethodInterceptor delegate = getDelegate(invocation.getThis(),invocation.getMethod());
        if (delegate != null) {
            return delegate.invoke(invocation);
        }
        else {
            return invocation.proceed();
        }
    }

    private MethodInterceptor getDelegate(Object target, Method method) {
        if (!this.delegates.containsKey(method)) {
            synchronized (this.delegates) {
                if (!this.delegates.containsKey(method)) {
                    MethodInterceptor delegate = new ValidateOperations(validateComponent);
                    this.delegates.put(method, delegate);
                }
            }
        }
        return this.delegates.get(method);
    }

    @Override
    public boolean implementsInterface(Class<?> intf) {
        return tech.ascs.cityworks.validate.base.Validatable.class.isAssignableFrom(intf);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
