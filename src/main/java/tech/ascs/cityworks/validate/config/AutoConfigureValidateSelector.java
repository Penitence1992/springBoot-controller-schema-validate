package tech.ascs.cityworks.validate.config;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import tech.ascs.cityworks.validate.base.EnableSchemaValidation;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 用于进行自动配置的选择器
 */
public class AutoConfigureValidateSelector extends AdviceModeImportSelector<EnableSchemaValidation>
//        extends SpringFactoryImportSelector<EnableSchemaValidation>
{

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        return new String[]{AutoConfigureValidate.class.getName()};

    }
}
