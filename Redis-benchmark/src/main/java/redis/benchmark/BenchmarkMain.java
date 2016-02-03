package redis.benchmark;

import java.util.*;

import com.beust.jcommander.JCommander;

import redis.benchmark.Benchmark;
import redis.benchmark.BenchmarkFactory;
import redis.benchmark.utils.BenchmarkLatencyResult;
import redis.benchmark.utils.BenchmarkCommandArguments;

public class BenchmarkMain {

	public static void main(String[] args) throws InterruptedException {

		BenchmarkCommandArguments cla = new BenchmarkCommandArguments();
		new JCommander(cla, args);
		Benchmark benchmark = BenchmarkFactory.getBenchmarkInstance(cla);

		List<BenchmarkLatencyResult> results = new ArrayList<BenchmarkLatencyResult>();
		for (int i = 0; i < cla.times; i++) {
			results.add(benchmark.runBenchmark());
			// sleep for a second for next run
			Thread.sleep(1000);
		}

		// print the mean latency result
		BenchmarkLatencyResult.getMeanResult(results).printResult();
		// shutdown benchmarking
		benchmark.shutdown();
	}
}
