package com.github.tkmybatis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.github.tkmybatis.dao")
@SpringBootApplication(scanBasePackages = {"com.github.tkmybatis"})
public class TkmybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TkmybatisApplication.class, args);
    }

}
