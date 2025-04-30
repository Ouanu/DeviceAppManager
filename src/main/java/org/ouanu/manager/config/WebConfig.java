package org.ouanu.manager.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置文件大小限制，这里设置为无限制，仅受系统内存限制
        factory.setMaxFileSize(DataSize.ofBytes(-1L));
        factory.setMaxRequestSize(DataSize.ofBytes(-1L));
        return factory.createMultipartConfig();
    }
}