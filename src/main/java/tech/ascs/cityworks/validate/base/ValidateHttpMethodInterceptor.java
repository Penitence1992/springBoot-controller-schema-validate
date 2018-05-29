package tech.ascs.cityworks.validate.base;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.web.server.WebFilter;

public interface ValidateHttpMethodInterceptor extends MethodInterceptor, WebFilter{
}
