package com.qzy;


import java.io.Serializable;


//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
public class Student implements Serializable {
    
    private String id;
    private String name;

    public Student(String id, String name) {
        this.id =id;
        this.name =name;
    }

    public void init(){
        System.out.println("hello...........");
    }
    
    public Student create(){
        return new Student("101","KK101");
    }
}
