package tech.ascs.cityworks.validate.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import tech.ascs.cityworks.validate.base.RequestHandlerValidate;
import tech.ascs.cityworks.validate.base.SchemaValidateComponent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractRequestHandlerMapBean implements RequestHandlerValidate {

    protected final Logger logger;
    protected final boolean isDebugEnable;

    protected AbstractRequestHandlerMapBean(Class cz) {
        logger = LoggerFactory.getLogger(cz);
        isDebugEnable = logger.isDebugEnabled();
    }

    protected Set<ValidationMessage> takeResult(SchemaValidateComponent component, JsonNode node){
        if (component != null) {
            return component.doValidate(node);
        } else {
            if(isDebugEnable){
                logger.debug("Not found SchemaValidateComponent");
            }
            return new HashSet<>();
        }
    }

    protected String generateKey(RequestMethod method, String url){
        Objects.requireNonNull(method,"RequestMethod can't be null");
        return method.toString() + ":" + url;
    }

}
