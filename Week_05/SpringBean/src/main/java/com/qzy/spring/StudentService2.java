package com.qzy.spring;

import org.springframework.stereotype.Component;

@Component
public class StudentService2 implements BaseStudentService{
    @Override
    public void study() {
        System.out.println("the student id is study");
    }
}
