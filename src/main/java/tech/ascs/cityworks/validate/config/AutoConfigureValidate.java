package tech.ascs.cityworks.validate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import tech.ascs.cityworks.validate.ValidateComponent;
import tech.ascs.cityworks.validate.ValidateInterceptor;
import tech.ascs.cityworks.validate.aspectj.ContainsMatchingPointcut;
import tech.ascs.cityworks.validate.base.RequestValidate;
import tech.ascs.cityworks.validate.base.ValidateMessageConvert;
import tech.ascs.cityworks.validate.convert.ChineseValidateMessageConvert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 自动装配Schema Validate的配置类
 */
public class AutoConfigureValidate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean IS_DEBUG_ENABLE = logger.isDebugEnabled();


    @Bean
    @ConfigurationProperties("schema")
    public SchemaValidateProperties schemaValidateProperties(){
        return new SchemaValidateProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateComponent")
    public RequestValidate validateComponent(ApplicationContext applicationContext,
                                             ObjectMapper mapper,
                                             SchemaValidateProperties properties){
        logger.info("No found bean {}, using default bean validateComponent",ValidateComponent.class.getName());
        return new ValidateComponent(properties,mapper,applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateMessageConvert")
    public ValidateMessageConvert validateMessageConvert(){
        return new ChineseValidateMessageConvert();
    }

    @Bean
    public DefaultBeanFactoryPointcutAdvisor validateAdvisor(SchemaValidateProperties properties) throws ClassNotFoundException {
        Set<Class<?>> methodAnnotation = findClassesFromNames(Arrays.asList(properties.getAnnotations()));
        ContainsMatchingPointcut pointcut = new ContainsMatchingPointcut(properties.getBasePackage(), methodAnnotation);
        DefaultBeanFactoryPointcutAdvisor advisor = new DefaultBeanFactoryPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdviceBeanName("validateInterceptor");
        return advisor;
    }
    @Bean
    @ConditionalOnMissingBean(name = "validateInterceptor")
    public MethodInterceptor validateInterceptor(RequestValidate validateComponent,
                                                 ValidateMessageConvert convert){
        logger.info("No found bean {}, using default bean validateInterceptor",ValidateInterceptor.class.getName());
        return new ValidateInterceptor(validateComponent, convert);
    }

    private Set<Class<?>> findClassesFromNames(Collection<String> annotations) throws ClassNotFoundException {
        Set<Class<?>> result = new HashSet<>();
        System.out.println(annotations);
        for (String clazzStr : annotations){
            if(IS_DEBUG_ENABLE){
                logger.debug("Find annotation for full path : {}", clazzStr);
            }
            result.add(Class.forName(clazzStr));
        }
        return result;
    }


}
