package example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.suyanliu.example.MainApplication;
import com.suyanliu.querycache.RedisOpt;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleTest {
	
	private Log log = LogFactory.getLog(ExampleTest.class);
	
	@Autowired
	private RedisOpt redisOpt;
	
	@Test
	public void test(){
		
		
		
		
		//String xx = redisOpt.get("xx").toString();
		//log.debug("xx"+xx);
		log.info("ffffffff");
		
		int bian = 50; //px
		int mid = 30; //px 30*2 照片间隔 60px = 5毫米。
		
		int cw = 1200 - bian * 2; // 100px  118 = 1cm 
		int ch = 1800 - bian * 2; // 100px  两边个 0.5cm
		
		int w = 295 + mid * 2; //照片尺寸
		int h = 413 + mid * 2; //照片尺寸
		
		int direct = 1; // 好片方向 上 2 为右
		
		log.info("w" + w + "h"+ h);
		
		int nw = cw / w;
		int nh = ch / h;

		int _nw = ch / w;
		int _nh = cw / h;

		log.info("nw" + nw + "nh"+ nh);
		log.info("_nw" + _nw + "_nh"+ _nh);

		if (_nw * _nh > nw * nh){
			direct = 2;
			nw = _nw;
			nh = _nh;
		}
		log.info("nw:" + nw + "nh:"+ nh+" direct:"+ direct);
		
		
	}

}
