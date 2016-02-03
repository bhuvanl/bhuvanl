package redis.nativeclient;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import redis.benchmark.hiredis.NativeRedis;

public class NativeRedisClientFactory implements PooledObjectFactory<NativeRedis> {

	protected String host;
	protected int port;

	public NativeRedisClientFactory(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void activateObject(PooledObject<NativeRedis> arg0) throws Exception {
		NativeRedis redis = arg0.getObject();
		if (null != redis)
			redis.connect();
	}

	@Override
	public void destroyObject(PooledObject<NativeRedis> arg0) throws Exception {
		NativeRedis redis = arg0.getObject();
		if (null != redis)
			redis.disconnect();

	}

	@Override
	public PooledObject<NativeRedis> makeObject() throws Exception {
		NativeRedis redis = new NativeRedis(this.host, this.port);
		redis.connect();
		return new DefaultPooledObject<NativeRedis>(redis);
	}

	@Override
	public void passivateObject(PooledObject<NativeRedis> arg0) throws Exception {
	}

	@Override
	public boolean validateObject(PooledObject<NativeRedis> arg0) {
		return true;
	}

}
