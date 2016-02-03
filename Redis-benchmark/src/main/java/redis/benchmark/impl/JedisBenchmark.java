package redis.benchmark.impl;

import akka.dispatch.Futures;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.benchmark.Benchmark;
import redis.benchmark.utils.BenchmarkExecutors;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.*;
import java.util.concurrent.Callable;

public class JedisBenchmark extends Benchmark {

	private static final Log LOG = LogFactory.getLog(JedisBenchmark.class);

	protected BenchmarkExecutors executor = new BenchmarkExecutors();
	protected static Object pool;

	protected static String key;
	protected String host = "localhost";
	protected int port = 6389;
	protected int noJedisConn = 10000;
	protected long startTime = 0l;

	public JedisBenchmark(final int noOps, final int noJedisConn, final String host, final int port, int dataSize) {
		super(noOps, dataSize);
		this.host = host;
		this.port = port;
		this.noJedisConn = noJedisConn;
	}

	public JedisBenchmark(final int requestPerSecond, final int duration, final int noJedisConn, final String host,
			final int port, int dataSize) {
		super(requestPerSecond, duration, dataSize);
		this.host = host;
		this.port = port;
		this.noJedisConn = noJedisConn;
	}

	public JedisBenchmark(final int noOps, int dataSize) {
		super(noOps, dataSize);
	}

	public JedisBenchmark(final int requestPerSecond, final int duration, int dataSize) {
		super(requestPerSecond, duration, dataSize);
	}

	protected void initPool() {
		JedisPoolConfig poolConfigObject = getPoolConfig(noJedisConn);
		pool = new JedisPool(poolConfigObject, host, port, 100);

	}

	protected JedisPoolConfig getPoolConfig(int n) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setTestOnBorrow(false);
		poolConfig.setMaxActive(n);
		poolConfig.setMaxIdle(n);
		poolConfig.setTestWhileIdle(false);
		poolConfig.setTestOnReturn(false);
		return poolConfig;
	}

	protected GenericObjectPoolConfig getGenericPoolConfig(int n) {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setTestOnBorrow(false);
		poolConfig.setMaxTotal(n);
		poolConfig.setMaxIdle(n);
		poolConfig.setTestWhileIdle(false);
		poolConfig.setTestOnReturn(false);
		return poolConfig;
	}

	protected void setData() {
		if (null == pool)
			initPool();
		JedisPool jedisPool = (JedisPool) pool;
		Jedis jedis = jedisPool.getResource();
		String key = generateRandomkey();
		JedisBenchmark.key = key;
		LOG.info("setting data on key: " + key);
		Map<String, String> data = new HashMap<String, String>();
		data.put(key, JedisBenchmark.data);
		jedis.hmset(key, data);

	}

	protected static String generateRandomkey() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static void setPool(Object inPool) {
		pool = inPool;
	}

	class BenchmarkTask implements Callable<Long> {

		public Long call() {
			long startTime = System.nanoTime();
			JedisPool jedisPool = (JedisPool) pool;
			Jedis jedis = jedisPool.getResource();
			try {
				Map<String, String> readData = jedis.hgetAll(JedisBenchmark.key);

				if (!readData.containsKey(JedisBenchmark.key))
					throw new Exception("invalid data Read from read");
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

	public void beforeRunbenchmark() {
		setData();
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	protected Callable<Long> getTask() {
		return new BenchmarkTask();
	}

	@Override
	public Iterable<Long> generateTasks(int requestsCount) {
		try {
			// run all tasks
			ArrayList<Future<Long>> allFutures = new ArrayList<Future<Long>>(requestsCount);
			for (int i = 0; i < requestsCount; i++) {
				allFutures.add(i, executor.getFuture(getTask()));
				executor.getFuture(new BenchmarkTask());
			}
			// wait for completion
			Future<Iterable<Long>> seq = Futures.sequence(allFutures, executor.getExecutionContext());
			Iterable<Long> ret = Await.result(seq, scala.concurrent.duration.Duration.Inf());
			return ret;

		} catch (Exception ex) {
			LOG.error("Exception while running  generating benchmark tasks ", ex);
			return null;
		}
	}
}
