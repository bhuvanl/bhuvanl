package redis.benchmark.utils;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

public class BenchmarkLatencyResult {

	static DecimalFormat doubleFormatter = new DecimalFormat("##.###");
	static DecimalFormat longFormatter = new DecimalFormat("###########");
	protected Map<String, Double> percentiles = new HashMap<String, Double>();
	protected static final String tp50 = "tp50";
	protected static final String tp90 = "tp90";
	protected static final String tp95 = "tp95";
	protected static final String tp99 = "tp99";
	protected static final String tp99_9 = "tp99_9";
	protected static final String tp100 = "tp100";
	public double requestPerSecond;

	public BenchmarkLatencyResult() {

	}

	public BenchmarkLatencyResult(Iterable<Long> times, long totalTimeToRun, long noOps) {
		List<Long> points = Lists.newArrayList(times);
		Collections.sort(points);
		this.requestPerSecond = noOps * 1000 / TimeUnit.NANOSECONDS.toMillis(totalTimeToRun);
		percentiles.put(tp50, (double) ((double) points.get((int) (points.size() / 2) - 1) / (double) 1000000));
		percentiles.put(tp90,
				(double) ((double) points.get((int) (points.size() * ((double) 90 / 100)) - 1) / (double) 1000000));
		percentiles.put(tp95,
				(double) ((double) points.get((int) (points.size() * ((double) 95 / 100)) - 1) / (double) 1000000));
		percentiles.put(tp99,
				(double) ((double) points.get((int) (points.size() * ((double) 99 / 100)) - 1) / (double) 1000000));
		percentiles.put(tp99_9,
				(double) ((double) points.get((int) (points.size() * ((double) 999 / 1000)) - 1) / (double) 1000000));
		percentiles.put(tp100, (double) ((double) points.get((int) points.size() - 1) / (double) 1000000));
	}

	public void printResult() {

		System.out.println("50 % <=" + doubleFormatter.format(percentiles.get(tp50)));
		System.out.println("90 % <=" + doubleFormatter.format(percentiles.get(tp90)));
		System.out.println("95 % <=" + doubleFormatter.format(percentiles.get(tp95)));
		System.out.println("99 % <=" + doubleFormatter.format(percentiles.get(tp99)));
		System.out.println("99.9 % <=" + doubleFormatter.format(percentiles.get(tp99_9)));
		System.out.println("100 % <=" + doubleFormatter.format(percentiles.get(tp100)));
		System.out.println(longFormatter.format(requestPerSecond) + " Requests per second");

	}

	public interface NumericProcessor {
		public double process(Collection<Double> values);
	}

	protected static NumericProcessor meanProcessor = new NumericProcessor() {

		@Override
		public double process(Collection<Double> values) {
			double sum = values.stream().reduce((x, y) -> {
				return x + y;
			}).get();

			return sum / values.size();
		}
	};

	protected static void updateMean(Collection<BenchmarkLatencyResult> results, BenchmarkLatencyResult returnResult,
			String meanLable) {
		results.forEach(k -> {
			List<Double> nums = new ArrayList<Double>();
			nums.add(k.percentiles.get(meanLable));
			returnResult.percentiles.put(meanLable, meanProcessor.process(nums));
		});
	}

	public static BenchmarkLatencyResult getMeanResult(Collection<BenchmarkLatencyResult> results) {

		BenchmarkLatencyResult meanLatency = new BenchmarkLatencyResult();
		updateMean(results, meanLatency, tp50);
		updateMean(results, meanLatency, tp90);
		updateMean(results, meanLatency, tp95);
		updateMean(results, meanLatency, tp99);
		updateMean(results, meanLatency, tp99_9);
		updateMean(results, meanLatency, tp100);

		for (BenchmarkLatencyResult result : results) {
			List<Double> nums = new ArrayList<Double>();
			nums.add(result.requestPerSecond);
			meanLatency.requestPerSecond = meanProcessor.process(nums);
		}

		return meanLatency;
	}
}
