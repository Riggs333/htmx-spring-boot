package io.github.wimdeblauwe.htmx.spring.boot.mvc;

import static io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader.HX_REFRESH;
import static io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader.HX_TRIGGER;
import static io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader.HX_TRIGGER_AFTER_SETTLE;
import static io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponseHeader.HX_TRIGGER_AFTER_SWAP;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HtmxHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            setHxTrigger(response, method);
            setHxRefresh(response, method);
            setVary(request, response);
        }

        return true;
    }

    private void setVary(HttpServletRequest request, HttpServletResponse response) {
        if (request.getHeader(HtmxRequestHeader.HX_REQUEST.getValue()) != null) {
            response.addHeader(HttpHeaders.VARY, HtmxRequestHeader.HX_REQUEST.getValue());
        }
    }

    private void setHxTrigger(HttpServletResponse response, Method method) {
        HxTrigger methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxTrigger.class);
        if (methodAnnotation != null) {
            response.setHeader(getHeaderName(methodAnnotation.lifecycle()), methodAnnotation.value());
        }
    }

    private void setHxRefresh(HttpServletResponse response, Method method) {
        HxRefresh methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, HxRefresh.class);
        if (methodAnnotation != null) {
            response.setHeader(HX_REFRESH.getValue(), "true");
        }
    }

    private String getHeaderName(HxTriggerLifecycle lifecycle) {
        switch (lifecycle) {
            case RECEIVE:
                return HX_TRIGGER.getValue();
            case SETTLE:
                return HX_TRIGGER_AFTER_SETTLE.getValue();
            case SWAP:
                return HX_TRIGGER_AFTER_SWAP.getValue();
            default:
                throw new IllegalArgumentException("Unknown lifecycle:" + lifecycle);
        }
    }
}
