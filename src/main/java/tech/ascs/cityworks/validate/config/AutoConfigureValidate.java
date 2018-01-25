package tech.ascs.cityworks.validate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import tech.ascs.cityworks.validate.ValidateComponent;
import tech.ascs.cityworks.validate.ValidateInterceptor;
import tech.ascs.cityworks.validate.base.ValidateMessageConvert;
import tech.ascs.cityworks.validate.convert.ChineseValidateMessageConvert;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 自动装配Schema Validate的配置类
 */
public class AutoConfigureValidate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${schema.path:classpath:/schema}")
    private String schemaBasePath;

    @Value("${schema.basePackage:tech.ascs.cityworks}")
    private String filterPackagePath;

    @Bean
    @ConditionalOnMissingBean(name = "validateComponent")
    public ValidateComponent validateComponent(ApplicationContext applicationContext, ObjectMapper mapper){
        logger.info("No found bean {}, using default bean validateComponent",ValidateComponent.class.getName());
        return new ValidateComponent(filterPackagePath,schemaBasePath,mapper,applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateMessageConvert")
    public ValidateMessageConvert validateMessageConvert(){
        return new ChineseValidateMessageConvert();
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateInterceptor")
    public ValidateInterceptor validateInterceptor(ValidateComponent validateComponent,
                                                   ValidateMessageConvert convert){
        logger.info("No found bean {}, using default bean validateInterceptor",ValidateInterceptor.class.getName());
        return new ValidateInterceptor(validateComponent, convert);
    }



}
