package com.qzy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Student.class)
public class TestStudent {

    @Resource
    private Student student;

    @Test
    public void test(){
        System.out.println(student.toString());
    }

}
