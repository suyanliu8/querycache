package com.suyanliu.querycache.impl;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.suyanliu.querycache.RedisOpt;

@Service
public class RedisOptImpl implements RedisOpt {
	private final Log log = LogFactory.getLog(RedisOptImpl.class);
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object value) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
         vo.set(key, value);
    }
    public void set(String key, Object value,long timeout,TimeUnit unit) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        vo.set(key, value, timeout, unit);
    }
    
    public Long increment(String key, Long delta) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        return vo.increment(key, delta);
    }
    
    public Double increment(String key, Double delta) {
    	ValueOperations<String,Object> vo = redisTemplate.opsForValue();
    	return vo.increment(key, delta);
    }
    
    public Set <String> findKeys(String pattern){
    	return redisTemplate.keys(pattern);
    }
    
    public Object get(String key) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        
        return vo.get(key);
    }
    
    public void hset(String key,String name , Object value) {
        HashOperations<String,String,Object> vo = redisTemplate.opsForHash();
        vo.put(key,name, value);
    }

    public List<Object> hmget(String key , Collection<String> hashKeys){
    	HashOperations<String,String,Object> vo = redisTemplate.opsForHash();
        return vo.multiGet(key, hashKeys);
    }
    
    public Object hget(String key,String name) {
        HashOperations<String,String,Object> vo = redisTemplate.opsForHash();
        return vo.get(key, name);
    }
	
    
    public Object hgetx(String key,String name) {
        HashOperations<String,String,Object> vo = redisTemplate.opsForHash();
        return vo.get(key, name);
    }
    
    public int hlen(String key) {
    	return redisTemplate.keys(key).size();
        
    }
    
    public long optCount(String key,long timeout){
    	ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        long num = vo.increment(key, 1);
    	redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        return num;
    }
    
    public boolean cangoto(String methodname ){
		Object obj =  hget("single_interface", methodname);
		Long v = 0l;
		if (obj instanceof Long){
			v = (Long)obj;
		}else if (obj instanceof Integer){
			v = Long.valueOf(((Integer)obj).intValue());
		}
		if (v == null || (v.longValue() < System.currentTimeMillis())){
			hset("single_interface", methodname, System.currentTimeMillis() + 1000*60*60);
			return true;
		}else{
			return false;
		}	
	}
		
	public void gotoend(String methodname ){
		hset("single_interface", methodname, 0l);
	}
    
    /**
     * 测试用，查找并且输出
     */
    public long LIUSY_findAndOut(String _key ,String cmd){
    	long count  = 0;
    	Set <String> sets = redisTemplate.keys(_key);
    	if ( sets != null){
        	Iterator <String >it =  sets.iterator();
        	while (it.hasNext()){
        		String val = it.next();
        		log.info("find view key:" + val );
        		
        		HashOperations<String,String,Object> vo2 = redisTemplate.opsForHash();
        		Set <String> sets2  = vo2.keys(val);
        		if (sets2 != null){
        			Iterator <String >it2 =  sets2.iterator();
                	while (it2.hasNext()){
                		String val2 = it2.next();
                		Object obj = vo2.get(val, val2);
                		log.info("find view val:"+val+" key2:" + val2 + "obj:" + obj);
                		if ( "update".equals(cmd) ){
                			vo2.put(val, val2, obj);
                		}
                	}
        		}
        		
        		
        		count ++;
        	}	
    	}
    	return count;
    	
    }
    
    /**
     * 批量设置过期时间
     */
    public long setExpires(String key , long timeout){
    	Set <String> sets = redisTemplate.keys(key);
    	long count  = 0;
    	Iterator <String >it =  sets.iterator();
    	
    	while (it.hasNext()){
    		String val = it.next();
    		boolean ret = redisTemplate.expire(val, timeout, TimeUnit.SECONDS);
    		if (ret){
    			log.info("ok" + ret + " :" + val );
    			count ++;
    		}else{
    			log.debug(" expire !"+val);
    		}
    		
    	}
    	return count;
    }
    
    /**
     * 取消过期，永久有效
     */
    public boolean persist(String key){
    	return redisTemplate.persist(key);
    }
    
    /*@Override
	public Object getUser(String sid) {
		return hget(sid, "userdata");
	}
	@Override
	public String getUserId(String sid) {
        return (String)hget(sid, MiniConstant.SESSION_USERID);
	}
	@Override
	public String getBusinessId(String sid) {
		return (String)hget(sid, MiniConstant.SESSION_BUSINESSID);
	}*/
	@Override
	public void removeKey(String sid) {
		redisTemplate.delete(sid);
	}

	@Override
	public Long increment(String key , String hashKey , Long delta) {
		HashOperations<String,String,Object> vo = redisTemplate.opsForHash();
		return vo.increment(key, hashKey, delta);
	}
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private String getThisDay() {
		return getThisDay(0);
	}
	private String getThisDay(int day) {
		return sdf.format(new java.util.Date(System.currentTimeMillis() - day * 60 * 60 * 1000 ));
	}
	/**
	 * 返回 成功条数
	 * @param key
	 * @param hashkeys
	 * @return
	 */
	public long hremoveKey(String key ,Object ...hashkeys)
	{
		HashOperations<String,String,Object> vo = redisTemplate.opsForHash();
        return vo.delete(key, hashkeys);
	}
	
	@Override
	public boolean setExpire(String key, long timeout, TimeUnit unit) {
		return redisTemplate.expire(key, timeout, unit);
	}
	
	/* 2019 liusy zset */
	public ZSetOperations<String , String> getZsetOper(){
		log.info("=================="+redisTemplate.equals(stringRedisTemplate));
		return stringRedisTemplate.opsForZSet();
	}
	/* 2019 liusy zset */
	
}