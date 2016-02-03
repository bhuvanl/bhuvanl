package redis.nativeclient;

import java.io.Closeable;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.benchmark.hiredis.NativeRedis;
import redis.clients.jedis.exceptions.JedisException;

public class NativeRedisClientPool implements Closeable {

	protected GenericObjectPool<NativeRedis> internalPool;

	public NativeRedisClientPool() {
	}

	public NativeRedisClientPool(final GenericObjectPoolConfig poolConfig, String host, int port) {
		NativeRedisClientFactory factory = new NativeRedisClientFactory(host, port);
		initPool(poolConfig, factory);
	}

	public void initPool(final GenericObjectPoolConfig poolConfig, PooledObjectFactory<NativeRedis> factory) {

		if (this.internalPool != null) {
			try {
				closeInternalPool();
			} catch (Exception e) {
			}
		}

		this.internalPool = new GenericObjectPool<NativeRedis>(factory, poolConfig);
	}

	protected void closeInternalPool() {
		try {
			internalPool.close();
		} catch (Exception e) {
			throw new JedisException("Could not destroy the pool", e);
		}
	}

	@Override
	public void close() {
		closeInternalPool();
	}

	public NativeRedis getResource() throws Exception {
		try {
			return internalPool.borrowObject();
		} catch (Exception e) {
			throw new Exception("Could not get a resource from the pool", e);
		}
	}

	@Deprecated
	public void returnResourceObject(final NativeRedis resource) throws Exception {
		if (resource == null) {
			return;
		}
		try {
			internalPool.returnObject(resource);
		} catch (Exception e) {
			throw new Exception("Could not return the resource to the pool", e);
		}
	}

	public void returnBrokenResource(final NativeRedis resource) {
		if (resource != null) {
			returnBrokenResourceObject(resource);
		}
	}

	public void returnResource(final NativeRedis resource) throws Exception {
		if (resource != null) {
			returnResourceObject(resource);
		}
	}

	public void destroy() {
		closeInternalPool();
	}

	protected void returnBrokenResourceObject(final NativeRedis resource) {
		try {
			internalPool.invalidateObject(resource);
		} catch (Exception e) {
			throw new JedisException("Could not return the resource to the pool", e);
		}
	}

	public int getNumActive() {
		if (this.internalPool == null || this.internalPool.isClosed()) {
			return -1;
		}

		return this.internalPool.getNumActive();
	}

	public int getNumIdle() {
		if (this.internalPool == null || this.internalPool.isClosed()) {
			return -1;
		}

		return this.internalPool.getNumIdle();
	}

	public int getNumWaiters() {
		if (this.internalPool == null || this.internalPool.isClosed()) {
			return -1;
		}

		return this.internalPool.getNumWaiters();
	}

}
