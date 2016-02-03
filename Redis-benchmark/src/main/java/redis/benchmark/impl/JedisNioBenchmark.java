package redis.benchmark.impl;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import nioredis.clients.jedis.Jedis;
import nioredis.clients.jedis.JedisPool;

public class JedisNioBenchmark extends JedisBenchmark {

	public JedisNioBenchmark(final int noOps, final int noJedisConn, final String host, final int port, int dataSize) {
		super(noOps, noJedisConn, host, port, dataSize);
		initPool();
	}

	public JedisNioBenchmark(final int qps, final int duration, final int noJedisConn, final String host,
			final int port, int dataSize) {
		super(qps, duration, noJedisConn, host, port, dataSize);
		initPool();
	}

	protected void initPool() {
		GenericObjectPoolConfig poolConfig = getGenericPoolConfig(super.noJedisConn);
		pool = new JedisPool(poolConfig, host, port, 100);
	}

	class BenchmarkTask implements Callable<Long> {

		public Long call() {
			long startTime = System.nanoTime();
			JedisPool jedisPool = (JedisPool) pool;
			Jedis jedis = jedisPool.getResource();
			try {
				Map<String, String> readData = jedis.hgetAll(JedisBenchmark.key);

				if (!readData.containsKey(JedisBenchmark.key))
					throw new Exception("invalid data Read from redis");

			} catch (Exception ex) {
				if (null != jedis) {
					jedisPool.returnBrokenResource(jedis);
					jedis = null;
				}
			} finally {
				if (null != jedis)
					jedisPool.returnResource(jedis);
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
