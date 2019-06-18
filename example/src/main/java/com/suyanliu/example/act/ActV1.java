package com.suyanliu.example.act;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

	
	
	@RequestMapping(value = "/u", method = RequestMethod.POST)
	public Object upload(@RequestParam("file") MultipartFile file , HttpServletRequest request) {
		if (file == null || file.equals("")) {
			return "{\"status\":\"-1\"}";
		}

		String fileName = file.getOriginalFilename();
		
		try {
			InputStream is = file.getInputStream();
			
			String basePath = "/data/pics/";
			
			File out = new File(basePath + fileName);
			FileOutputStream fos = new FileOutputStream(out);
			do {
				int b = is.read();
				if (b != -1){
					fos.write(b);
				}else{
					break;
				}
			} while (true);
			
			fos.flush();
			fos.close();
			
			return "";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{\"status\":\"-1\"}";
	
	}
}
