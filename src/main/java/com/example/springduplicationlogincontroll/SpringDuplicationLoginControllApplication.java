package com.example.springduplicationlogincontroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class SpringDuplicationLoginControllApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDuplicationLoginControllApplication.class, args);
    }

}
