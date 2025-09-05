package io.ortis.jsak.concurrency;

import io.ortis.jsak.concurrency.bus.InboundMessageBus;
import io.ortis.jsak.util.InputValidator;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ParallelWorker<T>
{
	private final Supplier<T> supplier;
	private final Consumer<T> consumer;
	private final int parallelism;
	private final Consumer<Throwable> exceptionHandler;

	private transient final ExecutorService executor;
	private transient final AtomicInteger shutdown = new AtomicInteger(0);

	public ParallelWorker(final Supplier<T> supplier, final Consumer<T> consumer, final int parallelism, final ThreadFactory threadFactory,
			final Consumer<Throwable> exceptionHandler)
	{
		this.supplier = InputValidator.checkNotNull(supplier, "supplier");
		this.consumer = InputValidator.checkNotNull(consumer, "consumer");
		this.parallelism = InputValidator.checkStrictlyPositive(parallelism, "parallelism");
		this.executor = Executors.newFixedThreadPool(parallelism, InputValidator.checkNotNull(threadFactory, "threadFactory"));
		this.exceptionHandler = InputValidator.checkNotNull(exceptionHandler, "exceptionHandler");
		for (int i = 0; i < parallelism; i++)
			this.executor.execute(this::process);
	}

	private void process()
	{
		while (!Thread.currentThread().isInterrupted() && shutdown.get() <= 1)
		{
			try
			{
				final T t = this.supplier.get();
				if (t == null)
				{
					if (this.shutdown.get() > 0)
						return;
					continue;
				}

				this.consumer.accept(t);

			} catch (final Throwable t)
			{
				this.exceptionHandler.accept(t);
			}
		}
	}

	public void shutdown()
	{
		if (!this.shutdown.compareAndSet(0, 1))
			return;
		this.executor.shutdown();
	}

	public void shutdownImmediately()
	{
		if (this.shutdown.compareAndSet(0, 2))
			this.executor.shutdown();
		this.shutdown.compareAndSet(1, 2);

	}

	public boolean awaitTermination(final Duration duration) throws InterruptedException
	{
		if (this.shutdown.get() <= 0)
			throw new IllegalStateException("Worker not shutdown");
		return this.executor.awaitTermination(duration.toNanos(), TimeUnit.NANOSECONDS);
	}

	public static <T> ParallelWorker.Builder<T> newBuilder()
	{
		return ParallelWorker.Builder.newBuilder();
	}

	public static class Builder<T>
	{
		private Supplier<T> supplier;
		private Consumer<T> consumer;

		private int parallelism;
		private ThreadFactory threadFactory;
		private Consumer<Throwable> exceptionHandler;

		public ParallelWorker.Builder<T> setSupplier(final Supplier<T> supplier)
		{
			this.supplier = supplier;
			return this;
		}

		public ParallelWorker.Builder<T> setSupplier(final BlockingQueue<T> queue, final Duration pollTimeout)
		{
			this.supplier = () -> {
				try
				{
					return queue.poll(pollTimeout.toNanos(), TimeUnit.NANOSECONDS);
				} catch (final InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return null;
				}
			};
			return this;
		}

		public ParallelWorker.Builder<T> setSupplier(final InboundMessageBus<T> bus, final Duration pollTimeout)
		{
			this.supplier = () -> bus.poll(pollTimeout);
			return this;
		}


		public ParallelWorker.Builder<T> setConsumer(final Consumer<T> consumer)
		{
			this.consumer = consumer;
			return this;
		}

		public ParallelWorker.Builder<T> setParallelism(final int parallelism)
		{
			this.parallelism = parallelism;
			return this;
		}

		public ParallelWorker.Builder<T> setThreadFactory(final ThreadFactory threadFactory)
		{
			this.threadFactory = threadFactory;
			return this;
		}

		public ParallelWorker.Builder<T> setExceptionHandler(final Consumer<Throwable> exceptionHandler)
		{
			this.exceptionHandler = exceptionHandler;
			return this;
		}

		public ParallelWorker<T> build()
		{
			return new ParallelWorker<>(supplier, consumer, parallelism, threadFactory, exceptionHandler);
		}

		public static <T> ParallelWorker.Builder<T> newBuilder()
		{
			return new ParallelWorker.Builder<>();
		}

	}
}
