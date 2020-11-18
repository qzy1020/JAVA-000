import com.qzy.spring.BaseStudentService;
import com.qzy.spring.StudentConfig;
import com.qzy.spring.StudentService2;
import com.qzy.spring.StudentStudy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StudentConfig.class)
public class TestSpringBean3 {
    @Autowired
    private BaseStudentService baseStudentService;
    @Test
    public void test(){
        baseStudentService.study();
    }
    @Autowired
    private StudentStudy studentStudy;
    @Test
    public void study(){
        studentStudy.study();
    }

}
