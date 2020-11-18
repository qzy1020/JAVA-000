package com.qzy;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Klass { 
    
    List<Student> students;
    
    public void dong(){
        System.out.println(this.getStudents());
    }

    public List<Student> getStudents() {
        students = new ArrayList<>();
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
