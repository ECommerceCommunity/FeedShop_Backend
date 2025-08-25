package com.cMall.feedShop.config;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 또는 "/**"
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);

        // 정적 파일에 대한 CORS 설정
        registry.addMapping("/images/**")

                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept");

        registry.addMapping("/uploads/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept");

        registry.addMapping("/files/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads 디렉토리에 대한 정적 파일 서빙 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // images 디렉토리에 대한 정적 파일 서빙 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");

        // files 디렉토리에 대한 정적 파일 서빙 설정
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:files/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // JSON converter for @RequestPart handling
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(new ObjectMapper());
        // Allow application/octet-stream to be treated as JSON for @RequestPart
        jsonConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM,
                new MediaType("application", "*+json")
        ));
        converters.add(jsonConverter);
    }
}