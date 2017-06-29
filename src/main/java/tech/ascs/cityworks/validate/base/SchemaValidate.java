package tech.ascs.cityworks.validate.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

/**
 * Created by RenJie on 2017/6/29 0029.
 * 校验接口,调用这个接口进行校验
 */
public interface SchemaValidate {

    Set<ValidationMessage> doValidate(JsonNode jsonNode);
}
