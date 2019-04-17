package example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.suyanliu.querycache.RedisOpt;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleTest {
	
	private Log log = LogFactory.getLog(ExampleTest.class);
	
	@Autowired
	private RedisOpt redisOpt;
	
	@Test
	public void test(){
		String xx = redisOpt.get("xx").toString();
		log.debug("xx"+xx);
		
	}

}
