package redis.benchmark.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.util.concurrent.MoreExecutors;
import com.lambdaworks.redis.LettuceFutures;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;

public class LettuceBenchmark extends JedisBenchmark {

	private static final Log LOG = LogFactory.getLog(LettuceBenchmark.class);

	public LettuceBenchmark(int noOps, int noJedisConn, String host, int port, int dataSize) {
		super(noOps, noJedisConn, host, port, dataSize);
	}

	public LettuceBenchmark(int qps, int duration, int noJedisConn, String host, int port, int dataSize) {
		super(qps, duration, noJedisConn, host, port, dataSize);
	}

	protected RedisClient getClient() {
		RedisClient client = new RedisClient(host, port);
		client.setDefaultTimeout(100, TimeUnit.MILLISECONDS);
		return client;
	}

	@Override
	public Iterable<Long> generateTasks(int requestsCount) {
		final ConcurrentLinkedQueue<Long> ret = new ConcurrentLinkedQueue<Long>();
		RedisClient client = getClient();
		List<RedisFuture<Map<String, String>>> futures = new ArrayList<RedisFuture<Map<String, String>>>();
		RedisAsyncConnection<String, String> asyncConnection = client.connectAsync();
		long time = System.nanoTime();
		for (int i = 0; i < requestsCount; i++) {

			final long startTime = System.nanoTime();
			final RedisFuture<Map<String, String>> result = asyncConnection.hgetall(JedisBenchmark.key);
			futures.add(i, result);
			result.addListener(new Runnable() {

				public void run() {
					try {
						Map<String, String> readData = result.get();
						if (!readData.containsKey(JedisBenchmark.key))
							throw new Exception("invalid data read from redis");
						long runTime = System.nanoTime() - startTime;
						ret.offer(runTime);
					} catch (Exception ex) {
						LOG.error("exception on get", ex);
					}

				}
			}, MoreExecutors.directExecutor());
		}
		LettuceFutures.awaitAll(1, TimeUnit.HOURS, futures.toArray(new RedisFuture[futures.size()]));
		totalNanoRunTime = System.nanoTime() - time;
		return ret;
	}

}
