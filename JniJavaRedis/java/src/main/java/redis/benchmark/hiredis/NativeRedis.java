package redis.benchmark.hiredis;

public class NativeRedis {

	protected native void helloRedis();

	protected native long connect(final String host, final int port);

	protected native String[] command(long connection, final String command);

	protected native void close(long connection);

	protected boolean connected = false;
	protected String hostName = "localhost";
	protected int port = 6379;
	protected long connectionPointer = -1l;

	static {
		System.loadLibrary("jniRedisNative");
	}

	public NativeRedis() {
	}

	public NativeRedis(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	public void connect() {
		if (!isConnected()) {
			this.connectionPointer = connect(this.hostName, this.port);
			this.connected = true;
		}
	}

	public void disconnect() {
		if (isConnected()) {
			close(this.connectionPointer);
			this.connected = false;
			this.connectionPointer = -1l;
		}
	}

	public boolean isConnected() {
		if (!connected)
			return false;
		if (connectionPointer == -1l)
			return false;
		return true;
	}

	public String[] execute(String commandString) {
		return command(this.connectionPointer, commandString);
	}

	public static void main(String[] args) {
		NativeRedis nativeRedis = new NativeRedis("127.0.0.1", 6379);
		nativeRedis.connect();
		String[] output = nativeRedis.execute("hgetall foo");
		for (String s : output)
			System.out.println(s);
		nativeRedis.disconnect();
	}
}