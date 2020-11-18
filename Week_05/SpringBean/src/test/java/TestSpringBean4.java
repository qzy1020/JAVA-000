import com.qzy.spring.BaseStudentService;
import com.qzy.spring.StudentConfig;
import com.qzy.spring.StudentConfigCreate;
import com.qzy.spring.StudentStudy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StudentConfig.class)
public class TestSpringBean4 {

    @Test
    public void test(){
        ApplicationContext context = new AnnotationConfigApplicationContext(StudentConfigCreate.class);
        StudentStudy studentStudy = context.getBean(StudentStudy.class);
    }
}
