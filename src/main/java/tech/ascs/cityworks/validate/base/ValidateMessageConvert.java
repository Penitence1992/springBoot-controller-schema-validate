package tech.ascs.cityworks.validate.base;

import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface ValidateMessageConvert {

    public Set<String> convert(Set<ValidationMessage> validationMessages);

}
