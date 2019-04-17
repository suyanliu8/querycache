package com.suyanliu.querycache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.StringUtils;

/**
 * 实现缓存的基础加载和缓存逻辑
 * @author liushuyou
 * @param <V>
 */
public abstract class QueryCacheBase<V> {
	private static final String LIUSYCACHE = "LIUSY:CACHE:V2";

	private final Log log = LogFactory.getLog(QueryCacheBase.class);
	
	public abstract RedisOpt getRedisOpt();
	
	/**
	 * 加载用户列表标示， false 是全部缓存， true 是只缓存活跃用户相关数据。
	 */
	public boolean loadActiveUserFlag = false;
	/**
	 * 加载排序列表，true 未加载， false 未不生成排序列表；
	 */
	private boolean loadSortFlag = true;
	/**
	 * 只缓存当前值
	 */
	private boolean onlyLoadCurrentFlag = false;
	
	
	public void init(boolean loadActiveUserFlag, boolean loadSortFlag){
		this.loadActiveUserFlag = loadActiveUserFlag;
		this.loadSortFlag = loadSortFlag;
	}
	
	public void init(boolean loadActiveUserFlag, boolean loadSortFlag ,boolean onlyLoadCurrentFlag){
		this.loadActiveUserFlag = loadActiveUserFlag;
		this.loadSortFlag = loadSortFlag;
		this.onlyLoadCurrentFlag = onlyLoadCurrentFlag;
	}
	
	/**
	 * 加载数据
	 * @param userId
	 * @return
	 */
	public abstract List<V> getAllData(String userId);
	
	/**
	 * 加载数据   需要考虑删除的内容，如何清理内存。 删除key 后set 暂时的方法。 
	 * @param key
	 * @param name
	 */
	public  void loaddata(String endkey,List<V> alls) {
		log.info(" loaddata "+endkey + alls);
		if (alls != null){
			ZSetOperations<String, String> zset = getRedisOpt().getZsetOper();
			getRedisOpt().removeKey(joinHashKey(endkey));
			if(loadSortFlag){
				getRedisOpt().removeKey(joinZsetKey(endkey));
			}
			alls.forEach((V v)->{
				saveOne(endkey, zset, v);
			});
			String cache_key = joinStatusKey(endkey);
			getRedisOpt().set(cache_key, "1");
		}else{
			log.error("{redis} loaddata 数据获取失败！"+ endkey);
		}
	}

	

	private V saveOne(String endkey, ZSetOperations<String, String> zset, V v) {
		if (StringUtils.isEmpty(endkey)){
			log.error(" {Cachetools saveOne }: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"+endkey + v.toString());
		}
		log.info(" {Cachetools saveOne }: "+endkey + v.toString());
		getRedisOpt().hset(joinHashKey(endkey), cacheName(v), v);
		if(loadSortFlag){
			zset.add(joinZsetKey(endkey), cacheName(v), getScore(v));
		}
		return v;
	}
	
	/**
	 * 加载用户所属内容。
	 * @param userId
	 * @param key
	 * @throws Exception
	 */
	public  void userLoaddata(String key,String userId) {
		log.info(" userLoaddata... user:"+userId + " key:"+key);
		List <V> alls = getAllData(userId);
		log.info(" getAllData:" +alls.size());
		if (alls != null){
			loaddata(joinKey(key, userId),alls);
		}else{
			log.error("{redis} userLoaddata 数据获取失败！"+ key+ "userId :"+userId);
		}
	}
	/**
	 * 排序字段
	 * @param v
	 * @return
	 */
	public abstract double getScore(V v) ;
	/**
	 * redis key 的组成 hash 集合存放的主键。例如： 订单，就是 ordercode  
	 * @param v
	 * @return
	 */
	public abstract String cacheName(V v);

	/**
	 * 定制任务加载数据
	 * @throws Exception 
	 */
	public void act_loaddata(String key) throws Exception{
		if (loadActiveUserFlag){
			Set <String> users = getActiveUsers();
			if (users != null){
				log.info(" active users count : "+users.size());
				users.forEach((String u)->{
					userLoaddata(key, u);
				});
			}else{
				log.info(" 无活跃用户！。。。");
			}
		}else{
			List <V> alls = getAllData(null);
			if (alls != null){
				loaddata(key ,alls);
			}else{
				log.error("{redis} act_loaddata 数据获取失败！"+ key);
			}
		}
	}
	
	/**
	 * 获取单个id值，所属用户
	 * @param key
	 * @param userId
	 * @param id
	 * @return
	 */
	public V getValueByUserId(String key, String userId ,String id) {
		return getCacheValue(key , userId, id, getRedisOpt());
		
	}
	/**
	 * 获得单个id值
	 * @param key
	 * @param id
	 * @return
	 */
	public V getValue(String key, String id) {
		return getCacheValue(key,null, id, getRedisOpt());
		
	}
	
	
	//  问题： 与用户相关的，或标示 用户相关的，立即加载，不异步加载，与用户无关的，异步加载。 
	public V getCacheValue(String key,String userId ,String cachename, RedisOpt rs){
		V val = (V)rs.hget(joinHashKey(joinKey(key, userId)), cachename);
		if (val == null){
			// 2019 03 05 解决，只想加载当前查询内容的情况。不考虑多加载，也不考虑用户纬度。
			if (onlyLoadCurrentFlag){
				val = findDbOne(cachename,userId);
				save(userId, val);
			}else{
				if (enabledLoad( key, userId, rs )){
					val = (V)rs.hget(joinHashKey(joinKey(key, userId)), cachename);
					log.info(" 阻塞加载后获取内容："+key + " "+ userId+" "+val);
					return val;
				};	
			}
		}
		return val;
		
	}
	
	public abstract V findDbOne(String key, String userId);

	/**
	 * 加载缓存。。。
	 * @param key
	 * @param userId
	 * @param rs
	 * @return
	 */
	private boolean enabledLoad(String key, String userId, RedisOpt rs) {
		log.error("cache: fail :enabledLoad: key - "+key  );
		String cache_key = joinStatusKey(joinKey(key, userId));
		// 获取缓存数据
		Object statuskey = rs.get(cache_key);
		// 判断是否有效。 
		if ("1".equals(statuskey)){ // 1 说明加载成功
			log.error(":---- 缓存无数据，可能是key或name错误！ 但，发现多了就可能被攻击了找老刘--------^_^ ^_^ ^_^ ^_^ ^_^ ^_^！"+cache_key);
			return false;
		}else if ("2".equals(statuskey)){ // 2 说明正在加载中。
			log.error(":----系统维护期间出现正常！----但这个不应该经常看到，多了需要找老刘---------^_^ ^_^ ^_^ ^_^ ^_^ ^_^！"+cache_key);
			return false;
		}else{
			log.info(" 加载------"+key + " "+ userId);
			if (loadActiveUserFlag){
				log.info(" 为缓存，阻塞加载。。。。。："+key + " "+ userId);
				userLoaddata(key,userId);
				return true;
			}else{
				log.info(" ThreadTaskUtils ------"+key + " "+ userId);
				// 说明未加载 启动加载！
				ThreadTaskUtils.run(new Runnable() {
		            @Override
		            public void run() {
		            	String ret = "未知异常！";
		            	try {
							rs.set(cache_key, "2");
							rs.setExpire(cache_key, 1200, TimeUnit.SECONDS);
							List <V> alls = getAllData(userId);
		            		loaddata(joinKey(key, userId),alls);
		            		ret = "1";
		            	}catch(Exception e){
		            		ret = e.getMessage();
							e.printStackTrace();
							log.error(" 加载失败：msg："+e.getMessage());
						} finally {
							rs.set(cache_key, ret);
							rs.persist(cache_key);
						}
		            }

		        });
			}
			
		}
		return false;
	}

	/**
	 * 分页查询
	 * @param key
	 * @param sort 1 倒序
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Object> queryBySort(String key , int sort ,int page, int size) {
		return query(sort, page, size, key);
	}

	private List<Object> query(int sort, int page, int size, String endkey) {
		log.info("{cache}:query"+endkey);
		page = page <= 0 ? 1:page;
		ZSetOperations<String, String> zset = getRedisOpt().getZsetOper();
		
		Set <String> keys = null;
		if (1 == sort){
			keys = zset.reverseRange(joinZsetKey(endkey), ( page - 1 ) * size, page * size - 1);
		}else{
			keys = zset.range(joinZsetKey(endkey), ( page - 1 ) * size, page * size - 1);
		}
		log.info("query:size :"+keys.size());
		List tmp =  getRedisOpt().hmget(joinHashKey(endkey), keys);
		log.info("query:list"+tmp.size());
		return tmp;
	}
	/**
	 * 查询数据总数
	 * @param string
	 * @param userId
	 * @return
	 */
	public Long getTotal(String key, String userId) {
		ZSetOperations<String, String> zset = getRedisOpt().getZsetOper();
		return zset.size(joinZsetKey(joinKey(key,userId)));
	}
	
	/**
	 * 倒序分页查询，用户自己内容，这个部分属于活跃的占少数，所以采用不欲缓存， 并且采用加失效时间。 
	 * @param userId
	 * @param sort 1 倒序
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Object> userQueryBySort(String key ,String userId,int sort , int page, int size) {
		log.info("{cache}:userQueryBySort"+key+userId);
		//维护用户活跃度
		refreshUser(userId);
		List <Object> list = query(sort, page, size, joinKey(key, userId));
		if (list == null || list.size() <=0 ){
			log.info("{缓存未查询到内容}"+userId + " "+ key);
			if (enabledLoad( key, userId, getRedisOpt() )){
				list = query(sort, page, size, joinKey(key, userId));
				log.info("querybysort- 阻塞加载后获取内容："+key + " "+ userId+" "+list);
				return list;
			}
		}else{
			log.info("query by sort {缓存被发现：}"+list.size());
		}
		return list;
	}

	
	/**
	 * 用户缓存保持十天
	 * @param userId
	 */
	public void refreshUser(String userId) {
		String key =  joinRefrehUser(userId);
		getRedisOpt().increment(key, 1L);
		getRedisOpt().setExpire(key, 10, TimeUnit.DAYS);
	}
	
	/**
	 * 缓存修改计数
	 * @param endkey
	 */
	public void modifiedCount(String endkey) {
		String key = joinModified(endkey);
		getRedisOpt().increment(key, 1L);
	}
	
	/**
	 * 获得 10天内活跃用户列表， 用户缓存相关数据。 
	 * @return
	 */
	public Set <String> getActiveUsers(){
		String key = joinRefrehUser("*");
		Set <String> users = getRedisOpt().findKeys(key);
		return users;
	}; 

	/**
	 * 处理 用户所属及非所属健值 endkey
	 * @param key
	 * @param userId
	 * @return
	 */
	public String joinKey(String key , String userId){
		return userId == null ? key : key+"_"+userId;
	}
	/**
	 * 拼接散列键值
	 * @param endkey
	 * @return
	 */
	private String joinHashKey(String endkey){
		return LIUSYCACHE+":hash:" + endkey;
	}
	/**
	 * 拼接有序键值
	 * @param endkey
	 * @return
	 */
	private String joinZsetKey(String endkey){
		return LIUSYCACHE+":zset:" + endkey;
	}
	
	/**
	 * 拼接状态键值
	 * @param endkey
	 * @return
	 */
	private String joinStatusKey(String endkey) {
		return LIUSYCACHE+":loadstatus:"+endkey;
	}
	/**
	 * 活用redis 主键名称
	 * @param userId
	 * @return
	 */
	public static String joinRefrehUser(String userId){
		return LIUSYCACHE + ":refresh:"+userId;
	}
	/**
	 * 缓存主键修改标示
	 * @param endkey
	 * @return
	 */
	public static String joinModified(String endkey){
		return LIUSYCACHE + ":modified:"+endkey;
	}
	
	
	public void delete(String userId, Object... values){
		log.info("{cacheTools}:delete"+userId + values);
		String key = getEndkey();
		String endkey = joinKey(key, userId);
		modifiedCount(endkey);	
		log.info("-------------CacheTools: delete: "+joinHashKey(endkey)+ " values"+values);
		
		getRedisOpt().hremoveKey(joinHashKey(endkey), values);
		if(loadSortFlag){
			log.info("{ cache tools } delete :"+values + " "+userId);
			ZSetOperations<String, String> zset = getRedisOpt().getZsetOper();
			zset.remove(joinZsetKey(endkey), values);
		} 
	}
	
	/**
	 * 缓存名称，一般使用表名
	 * @return
	 */
	public abstract String getEndkey();
	
	/**
	 * 添加或更新
	 * @param tmp
	 * @return
	 */
	public V save(String userId,V v) {
		if (v == null){
			log.error("{m:CacheTools.save,msg: v==null}---");
			return v;
		}else{
			String key = getEndkey();
			String endkey = joinKey(key, userId);
			modifiedCount(endkey);
			ZSetOperations<String, String> zset = getRedisOpt().getZsetOper();
			return saveOne(endkey, zset, v);	
		}
		
	}
	
	/**
	 * 批量添加或更新  不能用于加载。 
	 * @param userId
	 * @param tmp
	 * @return
	 */
	public Iterable<V> save(String userId, Iterable<V> tmp) {
		log.info(" {Cachetools save }: "+userId + tmp);
		ZSetOperations<String, String> zset = getRedisOpt().getZsetOper();
		tmp.forEach((V v)->{
			saveOne(joinKey(getEndkey(), userId), zset, v);
		});
		return tmp;
	}
	
	
	/**
	 * 是否启用redis cache  防止测试期间影响支付。
	 * @param endkey
	 * @return
	 */
	public static boolean enabledRedisCache(String endkey){
		return  true;
	}
	
}
