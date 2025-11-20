package com.cartapi.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class SessionCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(httpResponse) {
            @Override
            public void addHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value.contains("JSESSIONID")) {
                    String fixed = value.replaceAll("(?i);\\s*Secure|;\\s*SameSite=[^;]+", "")
                            + "; SameSite=None; Secure";
                    super.addHeader(name, fixed);
                } else {
                    super.addHeader(name, value);
                }
            }
        };

        chain.doFilter(request, wrapper);
    }
}