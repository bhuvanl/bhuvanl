package redis.inmemory.jedis;

import redis.clients.jedis.Client;

public class InmemoryJedisClient extends Client {

	String host = "localhost";

	public InmemoryJedisClient(String host) {
		super(host);
	}

	@Override
	public String getHost() {
		return host;
	}
}
