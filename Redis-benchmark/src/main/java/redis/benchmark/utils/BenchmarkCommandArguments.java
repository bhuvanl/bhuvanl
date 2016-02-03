package redis.benchmark.utils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

import java.util.List;

public class BenchmarkCommandArguments {

	@Parameter
	public List<String> parameters = Lists.newArrayList();

	@Parameter(names = "-n", description = "#operations")
	public Integer noOps = 100000;

	@Parameter(names = "-c", description = "#connections")
	public Integer noConnections = 1;

	@Parameter(names = "-h", description = "Host")
	public String host = "localhost";

	@Parameter(names = "-p", description = "port")
	public Integer port = 6379;

	@Parameter(names = "-s", description = "data size (bytes)")
	public Integer dataSize = 100;

	/**
	 * supported types 
	 * lettuce 
	 * lettuceunix
	 * jedisnio 
	 * jedis
	 * nativeredis
	 * default support type = jedis
	 */
	@Parameter(names = "-type", description = "type of the test")
	public String type = "";

	@Parameter(names = "-times", description = "number of times to run the test")
	public int  times = 1;
	
	@Parameter(names = "-qps", description = "qps")
	public int  qps = -1;
	
	@Parameter(names = "-duration", description = "duration in seconds")
	public int  duration = 1;
	
}
