package redis.benchmark.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import nioredis.clients.jedis.Jedis;

import redis.benchmark.hiredis.NativeRedis;
import redis.nativeclient.NativeRedisClientPool;

public class NativeRedisBenchmark extends JedisBenchmark {

	private static final Log LOG = LogFactory.getLog(NativeRedisBenchmark.class);
	protected static final String HGETALL = "hgetall";
	protected static final String CMD_SEP = " ";
	protected String cmdString = "";

	public NativeRedisBenchmark(int requestPerSecond, int duration, int noJedisConn, String host, int port,
			int dataSize) {
		super(requestPerSecond, duration, noJedisConn, host, port, dataSize);
		initPool();
	}

	public NativeRedisBenchmark(int requestPerSecond, int duration, int dataSize) {
		super(requestPerSecond, duration, dataSize);
		initPool();
	}

	public NativeRedisBenchmark(int noOps, int noJedisConn, String host, int port, int dataSize) {
		super(noOps, noJedisConn, host, port, dataSize);
		initPool();
	}

	public NativeRedisBenchmark(int noOps, int dataSize) {
		super(noOps, dataSize);
		initPool();
	}

	protected void initPool() {
		GenericObjectPoolConfig poolConfig = getGenericPoolConfig(super.noJedisConn);
		pool = new NativeRedisClientPool(poolConfig, host, port);
	}

	// native redis client do not support set command - hence using Jedis for
	// setting the data
	protected void setData() {
		Jedis jedis = new Jedis(host, port);
		String key = generateRandomkey();
		JedisBenchmark.key = key;
		LOG.info("setting data on key: " + key);
		Map<String, String> data = new HashMap<String, String>();
		data.put(key, JedisBenchmark.data);
		jedis.hmset(key, data);
		jedis.close();
		// set the cmdString to use randomkey Generated
		cmdString = HGETALL + CMD_SEP + JedisBenchmark.key;
	}

	class BenchmarkTask implements Callable<Long> {

		public Long call() {
			long startTime = System.nanoTime();
			NativeRedisClientPool redisPool = (NativeRedisClientPool) pool;
			NativeRedis redis = null;
			try {
				redis = redisPool.getResource();
				String[] readData = redis.execute(cmdString);
				Arrays.sort(readData);
				if (Arrays.binarySearch(readData, JedisBenchmark.key) == -1)
					throw new Exception("invalid data Read from redis");

			} catch (Exception ex) {
				if (redis != null) {
					redisPool.returnBrokenResource(redis);
					redis = null;
				}
			} finally {
				try {
					if (redis != null) {
						redisPool.returnResource(redis);
						redis = null;
					}
				} catch (Exception ex) {

				}
			}
			long runTime = System.nanoTime() - startTime;
			return runTime;

		}
	}

	@Override
	protected Callable<Long> getTask() {
		return new BenchmarkTask();
	}
}
