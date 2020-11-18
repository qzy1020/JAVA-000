package com.qzy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConditionalOnClass(Student.class)
@EnableConfigurationProperties(StudentProperties.class)
public class StudentConfiguration {

    @Resource
    private StudentProperties studentProperties;

    @Bean
    @ConditionalOnMissingBean(Student.class)
    @ConditionalOnProperty(prefix = "student", value = "enabled", havingValue = "true")
    public Student student(){
        return new Student(studentProperties.getId(), studentProperties.getName());
    }
}
