package example.act;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.suyanliu.querycache.RedisOpt;

@RestController
@RequestMapping(path = "/v1")
public class ActV1 {
	
	@Autowired
	private RedisOpt redisOpt;
	
	@RequestMapping(path = "/a", method = RequestMethod.GET)
	public Object a(){
		redisOpt.set("xx", "fff");
		String x = redisOpt.get("xx").toString();
		return "xx"+ redisOpt + x;
	}

}
