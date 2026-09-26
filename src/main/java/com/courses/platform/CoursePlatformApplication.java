package com.courses.platform;

import com.courses.platform.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class CoursePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoursePlatformApplication.class, args);
    }
}