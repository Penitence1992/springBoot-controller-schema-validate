package tech.ascs.cityworks.validate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import tech.ascs.cityworks.validate.ValidateComponent;
import tech.ascs.cityworks.validate.base.AnnotationOperation;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 自动装配Schema Validate的配置类
 */
@SuppressWarnings("unchecked")
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
    @ConditionalOnMissingBean(name = "annotationOperation")
    public AnnotationOperation annotationOperation(){
        return () -> Sets.newHashSet(RequestMapping.class, GetMapping.class, PostMapping.class,
                DeleteMapping.class, PutMapping.class, PatchMapping.class);
    }

/*    @Bean
    @ConditionalOnMissingBean(name = "validateInterceptor")
    public ValidateInterceptor validateInterceptor(ValidateComponent validateComponent){
        logger.info("No found bean {}, using default bean validateInterceptor",ValidateInterceptor.class.getName());
        return new ValidateInterceptor(validateComponent);
    }*/

}
