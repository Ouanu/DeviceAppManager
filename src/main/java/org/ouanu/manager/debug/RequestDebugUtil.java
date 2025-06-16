package org.ouanu.manager.debug;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

public class RequestDebugUtil {

    public static Map<String, Object> dumpRequest(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();

        // 1. 基础信息
        requestInfo.put("Method", request.getMethod());
        requestInfo.put("RequestURI", request.getRequestURI());
        requestInfo.put("RequestURL", request.getRequestURL().toString());
        requestInfo.put("Protocol", request.getProtocol());
        requestInfo.put("Scheme", request.getScheme());
        requestInfo.put("Secure", request.isSecure());

        // 2. 客户端信息
        requestInfo.put("RemoteAddr", request.getRemoteAddr());
        requestInfo.put("RemoteHost", request.getRemoteHost());
        requestInfo.put("RemotePort", request.getRemotePort());
        requestInfo.put("RemoteUser", request.getRemoteUser());

        // 3. 路径信息
        requestInfo.put("ContextPath", request.getContextPath());
        requestInfo.put("ServletPath", request.getServletPath());
        requestInfo.put("PathInfo", request.getPathInfo());
        requestInfo.put("QueryString", request.getQueryString());

        // 4. 头部信息
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        requestInfo.put("Headers", headers);

        // 5. 参数信息
        Map<String, String[]> parameters = request.getParameterMap();
        requestInfo.put("Parameters", parameters);

        // 6. 属性信息
        Map<String, Object> attributes = new HashMap<>();
        Enumeration<String> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String name = attrNames.nextElement();
            attributes.put(name, request.getAttribute(name));
        }
        requestInfo.put("Attributes", attributes);

        // 7. 其他信息
        requestInfo.put("ContentType", request.getContentType());
        requestInfo.put("ContentLength", request.getContentLength());
        requestInfo.put("CharacterEncoding", request.getCharacterEncoding());
        requestInfo.put("Locale", request.getLocale());
        requestInfo.put("Locales", Collections.list(request.getLocales()));

        // 8. Cookie信息
        if (request.getCookies() != null) {
            Map<String, String> cookies = new HashMap<>();
            for (Cookie cookie : request.getCookies()) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
            requestInfo.put("Cookies", cookies);
        }

        return requestInfo;
    }

    public static String formatRequestDump(HttpServletRequest request) {
        Map<String, Object> dump = dumpRequest(request);
        StringBuilder sb = new StringBuilder("\n===== HTTP Request Dump =====\n");

        dump.forEach((category, value) -> {
            sb.append(String.format("\n[%s]\n", category.toUpperCase()));
            if (value instanceof Map) {
                ((Map<?, ?>) value).forEach((k, v) -> {
                    sb.append(String.format("  %-20s = %s\n", k,
                            v instanceof String[] ? Arrays.toString((String[])v) : v));
                });
            } else {
                sb.append(String.format("  %s\n", value));
            }
        });

        return sb.append("\n===== End Dump =====\n").toString();
    }
}
