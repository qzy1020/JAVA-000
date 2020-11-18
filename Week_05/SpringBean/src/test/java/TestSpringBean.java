import com.qzy.spring.*;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;

public class TestSpringBean {
    @Test
    public void  testBean(){
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

        //构造器注入
        StudentServiceImpl1 studentService1 = context.getBean(StudentServiceImpl1.class);
        studentService1.execute();
        System.out.println(studentService1.getClass());

        //属性注入
        StudentServiceImpl2 studentService2 = context.getBean(StudentServiceImpl2.class);
        studentService2.execute();
        System.out.println(studentService2.getClass());

        //接口注入
        BaseStudentService baseStudentService = context.getBean(BaseStudentService.class);
        baseStudentService.study();
        System.out.println(baseStudentService.getClass());

    }

}
