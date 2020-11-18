package com.qzy.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentStudy {
    private BaseStudentService baseStudentService;
    @Autowired
    public StudentStudy(BaseStudentService baseStudentService){
        this.baseStudentService = baseStudentService;
    }

    public StudentStudy() {
        System.out.println("StudentStudy is init...");
    }

    public void study(){
        baseStudentService.study();
    }

}
