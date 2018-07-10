package tech.ascs.cityworks.validate.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;

public class DisablingJsonIgnoreIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public boolean isAnnotationBundle(Annotation ann) {
        // 忽略JsonIgnore属性和 忽略JsonIgnoreProperties
        if (JsonIgnore.class.equals(ann.annotationType()) || JsonIgnoreProperties.class.equals(ann.annotationType())) {
            return false;
        }
        return super.isAnnotationBundle(ann);
    }

    //强制设定为读写权限
    @Override
    public JsonProperty.Access findPropertyAccess(Annotated m) {
        return JsonProperty.Access.READ_WRITE;
    }
}
