package redis.inmemory.jedis;

import java.util.Map;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class InmemoryPipeline extends Pipeline {

	private InmemoryJedis jediser;

	public InmemoryPipeline(InmemoryJedis jediser) {
		this.jediser = jediser;
	}

	public Response<String> hmset(String key, Map<String, String> hash) {
		jediser.hmset(key, hash);
		return null;
	}

	public Response<Long> expireAt(String key, long unixTime) {
		jediser.expireAt(key, unixTime);
		return null;
	}

	public Response<Long> hincrBy(String key, String field, long value) {
		jediser.hincrBy(key, field, value);
		return null;
	}

	public void sync() {
		return;
	}
}
