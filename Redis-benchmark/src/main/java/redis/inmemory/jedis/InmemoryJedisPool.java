
package redis.inmemory.jedis;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class InmemoryJedisPool extends JedisPool {

	private InmemoryJedis jedis;

	public InmemoryJedisPool(JedisPoolConfig poolConfig, String cname, int db) {
		super(poolConfig, cname, 6389, 100);
		// super(poolConfig, cname, 6389, 0, (String) null,db);
		jedis = new InmemoryJedis("localhost");
	}

	@Override
	public Jedis getResource() {
		return jedis;
	}

	public void returnBrokenResource(final BinaryJedis resource) {
		// do nothing
	}

	public void returnResource(final BinaryJedis resource) {
		// do nothing.
	}

}
