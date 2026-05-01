package com.bqy.openapibackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 请求体缓存 Filter
 *
 * <p>对所有请求使用 {@link ContentCachingRequestWrapper} 包装，
 * 使得请求体（InputStream）可以被多次读取。
 *
 * <p>这解决了 API Key 认证拦截器（AOP）读取请求体后，
 * Controller 层无法再次读取请求体的问题。
 */
@Component
public class RequestCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            // 若还未包装，则用 ContentCachingRequestWrapper 包装
            if (!(httpRequest instanceof ContentCachingRequestWrapper)) {
                ContentCachingRequestWrapper wrappedRequest =
                        new ContentCachingRequestWrapper(httpRequest);
                chain.doFilter(wrappedRequest, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

