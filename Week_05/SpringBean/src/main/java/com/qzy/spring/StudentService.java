package com.qzy.spring;


public class StudentService implements BaseStudentService{
    @Override
    public void study() {
        System.out.println("the student id is study");
    }
}
