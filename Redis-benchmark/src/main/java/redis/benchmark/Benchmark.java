package redis.benchmark;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.benchmark.utils.BenchmarkLatencyResult;

public abstract class Benchmark {

	private static final Log LOG = LogFactory.getLog(Benchmark.class);

	protected int noOps_;
	protected int dataSize;
	protected long totalNanoRunTime;
	protected int desiredRequestsPerSecond;
	protected int duration;
	protected static String data;

	protected Benchmark(int noOps_, int dataSize) {
		this.noOps_ = noOps_;
		this.dataSize = dataSize;
		this.desiredRequestsPerSecond = -1;
		this.duration = -1;
		data = RandomStringUtils.random(dataSize);
	}

	protected Benchmark(int requestsPerSecond, int duration, int dataSize) {
		this.noOps_ = -1;
		this.duration = duration;
		this.desiredRequestsPerSecond = requestsPerSecond;
		this.dataSize = dataSize;
		data = RandomStringUtils.random(dataSize);
	}

	public abstract Iterable<Long> generateTasks(int requestsCount);

	public void beforeRunbenchmark() {
	}

	public BenchmarkLatencyResult runBenchmark() throws InterruptedException {
		beforeRunbenchmark();

		if (desiredRequestsPerSecond == -1) {
			long startTime = System.nanoTime();
			Iterable<Long> runTimes = generateTasks(noOps_);
			long timeToRun = System.nanoTime() - startTime;
			return benchMarklatencyResults(runTimes, timeToRun);
		}

		class BenchmarkTask extends TimerTask {
			int requests;
			Timer timer = new Timer();
			ConcurrentLinkedQueue<Long> taskRunTimes = new ConcurrentLinkedQueue<Long>();
			int times;
			boolean benchmarkComplete = false;
			long totalTimeTorun = -1;

			BenchmarkTask(int requests, int times) {
				this.requests = requests;
				this.times = times;
				timer.schedule(this, 1000, 1000);
			}

			@Override
			public void run() {
				LOG.info("Started run " + times);
				long startTime = System.nanoTime();
				Iterable<Long> runTimes = generateTasks(requests);
				long timeToRun = System.nanoTime() - startTime;
				totalTimeTorun += timeToRun;
				Iterator<Long> itr = runTimes.iterator();
				while (itr.hasNext()) {
					taskRunTimes.offer(itr.next());
				}
				LOG.info("completed run " + times);
				times--;
				if (times == 0) {
					benchmarkComplete = true;
					timer.cancel();
				}

			}

			public boolean isBenchmarkComplete() {
				return benchmarkComplete;
			}

			public Iterable<Long> getTaskRunTimes() {
				return taskRunTimes;
			}

			public long getTotalRunTime() {
				return totalTimeTorun;
			}
		}

		BenchmarkTask benchmark = new BenchmarkTask(desiredRequestsPerSecond, duration);
		while (!benchmark.isBenchmarkComplete())
			Thread.sleep(1000);
		return benchMarklatencyResults(benchmark.getTaskRunTimes(), benchmark.getTotalRunTime());

	}

	public abstract void shutdown();

	public BenchmarkLatencyResult benchMarklatencyResults(Iterable<Long> times, long runTime) {
		if (null == times) {
			System.out.println((noOps_ * 1000 / TimeUnit.NANOSECONDS.toMillis(runTime)) + " Operations per second");
			return new BenchmarkLatencyResult();
		}
		int numberOfRequests = noOps_ == -1 ? desiredRequestsPerSecond * duration : noOps_;
		System.out.println("Data size :" + dataSize);
		System.out.println("Number of ops: " + numberOfRequests);
		System.out.println("Time Test Ran for (ms) : " + TimeUnit.NANOSECONDS.toMillis(runTime));
		BenchmarkLatencyResult ret = new BenchmarkLatencyResult(times, runTime, numberOfRequests);

		return ret;
	}
}