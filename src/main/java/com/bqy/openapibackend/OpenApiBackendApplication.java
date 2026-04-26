package com.bqy.openapibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bqy.openapibackend.mapper")
public class OpenApiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiBackendApplication.class, args);
    }

}
