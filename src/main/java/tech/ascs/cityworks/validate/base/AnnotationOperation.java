package tech.ascs.cityworks.validate.base;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Created by RenJie on 2017/6/30 0030.
 */
public interface AnnotationOperation {

    Set<Class<? extends Annotation>> getCutAnnotation();
}
