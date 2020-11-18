package com.qzy.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentConfigCreate {
    @Bean
    public StudentStudy create(){
        return new StudentStudy();
    }
}
