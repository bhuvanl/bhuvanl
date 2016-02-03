package redis.benchmark.impl;

import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

public class LettuceUnixSocket extends LettuceBenchmark {

	public LettuceUnixSocket(int noOps, int noJedisConn, String host, int port, int dataSize) {
		super(noOps, noJedisConn, host, port, dataSize);
	}

	public LettuceUnixSocket(int qps, int duration, int noJedisConn, String host, int port, int dataSize) {
		super(qps, duration, noJedisConn, host, port, dataSize);
	}

	@Override
	protected RedisClient getClient() {
		RedisURI redisUri = RedisURI.create("redis-socket:///tmp/redis");
		RedisClient client = new RedisClient(redisUri);
		client.setDefaultTimeout(100, TimeUnit.MILLISECONDS);
		return client;
	}

}
