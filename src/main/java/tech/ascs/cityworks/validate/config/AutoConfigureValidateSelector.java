package tech.ascs.cityworks.validate.config;

import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import tech.ascs.cityworks.validate.base.EnableSchemaValidation;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 用于进行自动配置的选择器
 */
public class AutoConfigureValidateSelector extends SpringFactoryImportSelector<EnableSchemaValidation> {

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        return new String[]{AutoConfigureValidate.class.getName()};
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }
}
