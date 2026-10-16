package com.yule.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.yule.ai.mapper")
@SpringBootApplication
public class AIBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIBoxApplication.class, args);
    }

}
