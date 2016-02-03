package redis.benchmark;

import redis.benchmark.impl.*;
import redis.benchmark.utils.BenchmarkCommandArguments;

public class BenchmarkFactory {

	public static Benchmark getBenchmarkInstance(BenchmarkCommandArguments args) {

		if (args.qps == -1) {
			switch (args.type) {
			case "lettuce":
				return new LettuceBenchmark(args.noOps, args.noConnections, args.host, args.port, args.dataSize);
			case "lettuceunix":
				return new LettuceUnixSocket(args.noOps, args.noConnections, args.host, args.port, args.dataSize);
			case "jedis":
				return new JedisBenchmark(args.noOps, args.noConnections, args.host, args.port, args.dataSize);
			case "jedisnio":
				return new JedisNioBenchmark(args.noOps, args.noConnections, args.host, args.port, args.dataSize);
			case "nativeredis":
				return new NativeRedisBenchmark(args.noOps, args.noConnections, args.host, args.port, args.dataSize);
			case "test":
				return new JedisBenchmark(args.noOps, args.dataSize);
			default:
				return new JedisBenchmark(args.noOps, args.noConnections, args.host, args.port, args.dataSize);
			}
		} else {
			switch (args.type) {
			case "lettuce":
				return new LettuceBenchmark(args.qps, args.duration, args.noConnections, args.host, args.port,
						args.dataSize);
			case "lettuceunix":
				return new LettuceUnixSocket(args.qps, args.duration, args.noConnections, args.host, args.port,
						args.dataSize);
			case "jedis":
				return new JedisBenchmark(args.qps, args.duration, args.noConnections, args.host, args.port,
						args.dataSize);
			case "jedisnio":
				return new JedisNioBenchmark(args.qps, args.duration, args.noConnections, args.host, args.port,
						args.dataSize);
			case "nativeredis":
				return new NativeRedisBenchmark(args.qps, args.duration, args.noConnections, args.host, args.port,
						args.dataSize);
			case "test":
				return new JedisBenchmark(args.qps, args.duration, args.dataSize);
			default:
				return new JedisBenchmark(args.qps, args.duration, args.noConnections, args.host, args.port,
						args.dataSize);
			}

		}

	}

}
