package redis.benchmark.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.*;

import akka.actor.ActorSystem;
import akka.dispatch.*;
import scala.concurrent.*;
import scala.concurrent.duration.Duration;

public class BenchmarkExecutors {

	protected static final Log LOG = LogFactory.getLog(BenchmarkExecutors.class);

	enum EXECUTOR_TYPE {
		FORK_AND_JOIN
	}

	static abstract class RedisExecutors {

		EXECUTOR_TYPE type;
		protected ExecutionContext executionContext;
		protected ExecutorService exService;

		private RedisExecutors(EXECUTOR_TYPE type) {
			this(type, null);
		}

		private RedisExecutors(EXECUTOR_TYPE type, ExecutorService exService) {
			this.type = type;
			this.exService = exService;
			if (exService != null)
				executionContext = ExecutionContexts.fromExecutorService(this.exService);
		}

		protected EXECUTOR_TYPE getType() {
			return type;
		}

		protected ExecutionContext getExecutionContext() {
			return executionContext;
		}

		protected void shutdown() {
			LOG.info("Shutdown Executor service called");
			try {
				if (exService != null) {
					exService.shutdown();
					exService.awaitTermination(10, TimeUnit.MINUTES);
				}
			} catch (Exception ex) {
				LOG.info("Exception on shutting down Executor Service", ex);
			}
		}
	}

	static class ForkAndJoinThreadPoolExecutor extends RedisExecutors {

		protected static final String SYSTEM_NAME = "system";
		protected ActorSystem actorSystem = null;

		public ForkAndJoinThreadPoolExecutor() {
			super(EXECUTOR_TYPE.FORK_AND_JOIN);
			actorSystem = akka.actor.ActorSystem.create(SYSTEM_NAME);
			executionContext = actorSystem.dispatcher();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void shutdown() {
			super.shutdown();
			if (actorSystem != null) {
				try {
					scala.concurrent.forkjoin.ForkJoinPool.commonPool().shutdown();
					scala.concurrent.forkjoin.ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.MINUTES);
				} catch (Exception ex) {

				}
				actorSystem.terminate();
				actorSystem.awaitTermination(Duration.Inf());
				LOG.info("Actor system termination status: " + actorSystem.isTerminated());
			}
		}
	}

	static class RedisExecutorFactory {

		protected static RedisExecutors getExecutor() {
			EXECUTOR_TYPE exType = EXECUTOR_TYPE.FORK_AND_JOIN;
			LOG.info("Executors Configured to use: " + exType.name());
			switch (exType) {
			case FORK_AND_JOIN:
			default:
				return new ForkAndJoinThreadPoolExecutor();
			}
		}
	}

	protected RedisExecutors executor;

	public BenchmarkExecutors() {
		executor = RedisExecutorFactory.getExecutor();
	}

	public Future<Long> getFuture(Callable<Long> task) {
		return Futures.future(task, executor.getExecutionContext());
	}

	public static <T> void waitForFuture(Future<T> f, int milliSecondsDuration) throws Exception {
		if (milliSecondsDuration != -1)
			Await.result(f, Duration.create(milliSecondsDuration, String.valueOf(milliSecondsDuration)));
		else
			Await.result(f, Duration.Inf());
	}

	public ExecutionContext getExecutionContext() {
		return executor.getExecutionContext();
	}

	public void shutdown() {
		executor.shutdown();
	}

}
