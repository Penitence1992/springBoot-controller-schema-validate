package tech.ascs.cityworks.validate.config;

import com.google.common.collect.Sets;
import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import tech.ascs.cityworks.validate.AnnotationValidatorOperationsInterceptor;
import tech.ascs.cityworks.validate.ValidateComponent;
import tech.ascs.cityworks.validate.base.AnnotationOperation;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by RenJie on 2017/6/30 0030.
 */
@Configuration
public class ValidateInterceptorConfiguration extends AbstractPointcutAdvisor implements IntroductionAdvisor, BeanFactoryAware {

    private Advice advice;

    private Pointcut pointcut;

    @Autowired
    private BeanFactory beanFactory;

    @PostConstruct
    public void init(){
        Set<Class<? extends Annotation>> validateAnnotationTypes = new LinkedHashSet<>(5);
        addAnnotationToSet(validateAnnotationTypes);
        this.pointcut = buildPointcut(validateAnnotationTypes);
        this.advice = buildAdvice();
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    @Override
    public ClassFilter getClassFilter() {
        return pointcut.getClassFilter();
    }

    @Override
    public void validateInterfaces() throws IllegalArgumentException {

    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public Class<?>[] getInterfaces() {
        return new Class[0];
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    protected Pointcut buildPointcut(Set<Class<? extends Annotation>> retryAnnotationTypes) {
        ComposablePointcut result = null;
        for (Class<? extends Annotation> retryAnnotationType : retryAnnotationTypes) {
            Pointcut filter = new AnnotationClassOrMethodPointcut(retryAnnotationType);
            if (result == null) {
                result = new ComposablePointcut(filter);
            }
            else {
                result.union(filter);
            }
        }
        return result;
    }

    protected Advice buildAdvice() {
        return new AnnotationValidatorOperationsInterceptor(beanFactory.getBean("validateComponent", ValidateComponent.class));
    }

    private void addAnnotationToSet(Set<Class<? extends Annotation>> validateAnnotationTypes){
        AnnotationOperation operation = beanFactory.getBean("annotationOperation", AnnotationOperation.class);
        if(operation != null){
            System.out.println(operation);
            validateAnnotationTypes.addAll(operation.getCutAnnotation());
        }else{
            validateAnnotationTypes.add(RequestMapping.class);
            validateAnnotationTypes.add(GetMapping.class);
            validateAnnotationTypes.add(PostMapping.class);
            validateAnnotationTypes.add(PutMapping.class);
            validateAnnotationTypes.add(PatchMapping.class);
            validateAnnotationTypes.add(DeleteMapping.class);
        }
    }

    private final class AnnotationClassOrMethodPointcut extends StaticMethodMatcherPointcut {

        private final MethodMatcher methodResolver;

        AnnotationClassOrMethodPointcut(Class<? extends Annotation> annotationType) {
            this.methodResolver = new AnnotationMethodMatcher(annotationType);
            setClassFilter(new AnnotationClassOrMethodFilter(annotationType));
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return getClassFilter().matches(targetClass) || this.methodResolver.matches(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AnnotationClassOrMethodPointcut)) {
                return false;
            }
            AnnotationClassOrMethodPointcut otherAdvisor = (AnnotationClassOrMethodPointcut) other;
            return ObjectUtils.nullSafeEquals(this.methodResolver, otherAdvisor.methodResolver);
        }

    }

    private final class AnnotationClassOrMethodFilter extends AnnotationClassFilter {

        private final AnnotationMethodsResolver methodResolver;

        AnnotationClassOrMethodFilter(Class<? extends Annotation> annotationType) {
            super(annotationType, true);
            this.methodResolver = new AnnotationMethodsResolver(annotationType);
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return super.matches(clazz) || this.methodResolver.hasAnnotatedMethods(clazz);
        }

    }

    private static class AnnotationMethodsResolver {

        private Class<? extends Annotation> annotationType;

        public AnnotationMethodsResolver(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public boolean hasAnnotatedMethods(Class<?> clazz) {
            final AtomicBoolean found = new AtomicBoolean(false);
            ReflectionUtils.doWithMethods(clazz,
                    method -> {
                        if (found.get()) {
                            return;
                        }
                        Annotation annotation = AnnotationUtils.findAnnotation(method,
                                annotationType);
                        if (annotation != null) { found.set(true); }
                    });
            return found.get();
        }

    }
}
