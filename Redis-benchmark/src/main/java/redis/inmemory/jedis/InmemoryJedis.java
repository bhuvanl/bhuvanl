package redis.inmemory.jedis;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class InmemoryJedis extends Jedis {

	public static class InMemoryRedisStore {

		protected Map<String, Map<String, String>> hashMaps = new ConcurrentHashMap<String, Map<String, String>>();

		public Map<String, String> hgetAll(String key) {
			Map<String, String> map = hashMaps.get(key);
			if (map == null) {
				map = new ConcurrentHashMap<String, String>();
			}
			return map;
		}

		public void hsetAll(String key, Map<String, String> attributes) {
			Map<String, String> tmp = hashMaps.get(key);
			if (tmp == null) {
				tmp = new ConcurrentHashMap<String, String>();
				hashMaps.put(key, tmp);
			}
			for (String tmpKey : attributes.keySet()) {
				hashMaps.get(key).put(tmpKey, attributes.get(tmpKey));
			}
		}

		public String hmset(String key, Map<String, String> hash) {
			hsetAll(key, hash);
			return null;
		}

	}

	private static InMemoryRedisStore imStore = new InMemoryRedisStore();
	private InmemoryJedisClient client;

	public InmemoryJedis(final String host) {
		super(host);
		client = new InmemoryJedisClient("localhost");
	}

	public Boolean exists(final String key) {
		return false;

	}

	public Set<String> smembers(final String key) {
		return null;
	}

	public Map<String, String> hgetAll(final String key) {
		return imStore.hgetAll(key);
	}

	public String get(final String key) {
		return StringUtils.EMPTY;
	}

	public Long ttl(final String key) {
		return 0L;
	}

	// default increment of only 1 is supported for now
	public Long hincrBy(final String key, final String field, final long value) {
		return 0L;
	}

	public Long persist(final String key) {
		return 0L;
	}

	public Long expireAt(final String key, final long unixTime) {
		return 0L;
	}

	@Override
	public String hmset(final String key, final Map<String, String> hash) {
		return imStore.hmset(key, hash);
	}

	public Pipeline pipelined() {
		InmemoryPipeline pipeline = new InmemoryPipeline(this);
		return pipeline;
	}

	@Override
	public Client getClient() {
		return client;
	}

}
