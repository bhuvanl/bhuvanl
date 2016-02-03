package redis.benchmark;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import redis.clients.jedis.JedisPoolConfig;
import redis.inmemory.jedis.InmemoryJedisPool;
import redis.benchmark.Benchmark;
import redis.benchmark.BenchmarkFactory;
import redis.benchmark.utils.BenchmarkLatencyResult;
import redis.benchmark.utils.BenchmarkCommandArguments;
import redis.benchmark.impl.JedisBenchmark;
import redis.clients.jedis.JedisPool;

public class BenchmarkTest extends TestCase {

	public BenchmarkTest(String testName) {
		super(testName);

	}

	public static Test suite() {
		return new TestSuite(BenchmarkTest.class);
	}

	public void testBenchmark() throws Exception {
		/* test with using nOps_ arguments */
		JedisPool pool = new InmemoryJedisPool(new JedisPoolConfig(), "localhost", 1);
		String[] args = { "-type", "test", "-s", "1024", "-n", "1000000" };
		BenchmarkCommandArguments cla = new BenchmarkCommandArguments();
		new JCommander(cla, args);
		Benchmark benchMark = BenchmarkFactory.getBenchmarkInstance(cla);
		JedisBenchmark.setPool(pool);
		List<BenchmarkLatencyResult> results = new ArrayList<BenchmarkLatencyResult>();
		for (int i = 0; i < cla.times; i++) {
			results.add(benchMark.runBenchmark());
			Thread.sleep(1000);
		}
		BenchmarkLatencyResult.getMeanResult(results).printResult();
		benchMark.shutdown();
		assertTrue(true);

		/* test with using qps and duration arguments */
		String[] args2 = { "-type", "test", "-s", "1024", "-qps", "100000", "-duration", "3" };
		cla = new BenchmarkCommandArguments();
		new JCommander(cla, args2);
		benchMark = BenchmarkFactory.getBenchmarkInstance(cla);
		JedisBenchmark.setPool(pool);
		results = new ArrayList<BenchmarkLatencyResult>();
		for (int i = 0; i < cla.times; i++) {
			results.add(benchMark.runBenchmark());
			Thread.sleep(1000);
		}
		BenchmarkLatencyResult.getMeanResult(results).printResult();
		benchMark.shutdown();
		assertTrue(true);

	}
}
