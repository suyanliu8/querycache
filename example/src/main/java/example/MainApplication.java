package example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.suyanliu.querycache.RedisOpt;
import com.suyanliu.querycache.impl.RedisOptImpl;

@SpringBootApplication
public class MainApplication {

	
	@Bean
    public RedisOpt redisService(){
    	return new RedisOptImpl(); 
    }
	
	@Autowired
	private RedisOpt redisOpt;
	
	
	public String redisget(){
		return redisOpt.get("xx").toString();
	}
	
	public MainApplication() {
		System.out.println(redisOpt);
	}
	
	public static void main(String[] args) {
		Object xx = SpringApplication.run(MainApplication.class, args);
		System.out.println(xx);
	}

}
