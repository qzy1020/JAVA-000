package com.qzy.spring;


public class StudentServiceImpl2 implements  BaseService{
    private String id;
    private String name;
    @Override
    public void execute() {
        System.out.println("the student id is : "+ id + ", " + "and the name is : "+name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
