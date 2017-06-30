package tech.ascs.cityworks.validate.base;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import tech.ascs.cityworks.validate.config.AutoConfigureValidateSelector;
import tech.ascs.cityworks.validate.config.ValidateInterceptorConfiguration;

import java.lang.annotation.*;

/**
 * Created by RenJie on 2017/6/29 0029.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AutoConfigureValidateSelector.class, ValidateInterceptorConfiguration.class})
@EnableAspectJAutoProxy
public @interface EnableSchemaValidation {

}
