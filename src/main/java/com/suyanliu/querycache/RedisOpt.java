package com.suyanliu.querycache;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.ZSetOperations;

public interface RedisOpt {

	public void set(String key, Object value);
	/**
	 * 有失效时间的set
	 * @param key
	 * @param value
	 * @param timeout
	 * @param unit
	 */
	public void set(String key, Object value,long timeout,TimeUnit unit);
	/**
	 * 自增
	 * @param key
	 * @param delta
	 */
	public Long increment(String key, Long delta);
	/**
	 * 自增
	 * @param key
	 * @param delta
	 */
	public Double increment(String key, Double delta);
	
	/**
	 * 获取匹配的Key列表
	 * @param pattern
	 * @return
	 */
	public Set <String> findKeys(String pattern);
	
	public Object get(String key);

	public void hset(String key, String name, Object value);

	public Object hget(String key, String name);

	/**
	 * 获取多个值。 
	 * @param key
	 * @param hashKeys
	 * @return
	 */
	public List<Object> hmget(String key , Collection<String> hashKeys);
	
	public void removeKey(String sid);

	public Long increment(String key, String hashkey, Long delta);

	public long setExpires(String key, long timeout);

	public long hremoveKey(String key, Object... hashkeys);

	/**
	 * 为指定的key设置过期时间
	 * 
	 * @param key
	 * @param timeout 过期时间
	 * @param unit 过期时间单位
	 * @return
	 */
	public boolean setExpire(String key, long timeout, TimeUnit unit);
	
	/**
	 * 取消过期，永久有效。
	 * @param key
	 * @return
	 */
	public boolean persist(String key);
	
	
	/**
	 * liusy zset oper
	 * @return
	 */
	public ZSetOperations<String , String> getZsetOper();
	

}