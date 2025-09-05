package io.ortis.jsak.concurrency;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FutureProvider<V> implements Consumer<V>, Supplier<V>, Future<V>
{
	private final AtomicReference<V> value = new AtomicReference<>(null);

	private final CountDownLatch latch = new CountDownLatch(1);

	public boolean isAvailable()
	{
		return latch.getCount() <= 0;
	}

	/**
	 * Sets the value. This method can only be called once.
	 * Once the value is set, any threads waiting in the get() methods will be unblocked.
	 *
	 * @param value The value to set.
	 * @throws IllegalStateException if the value has already been set.
	 */
	public void set(final V value)
	{
		if (this.value.compareAndSet(null, value))
			latch.countDown();
		else
			throw new IllegalStateException("Value has already been set");
	}

	/**
	 * Waits for the value to become available and retrieves it.
	 * This method will block until the timeout is reached or the value is set.
	 *
	 * @param timeout The maximum duration to wait for the value.
	 * @return The value of type T.
	 * @throws TimeoutException     if the timeout is reached before the value is set.
	 * @throws InterruptedException if the waiting thread is interrupted.
	 */
	public V get(final Duration timeout) throws TimeoutException, InterruptedException
	{
		if (isAvailable() || latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS))
			return value.get();
		else
			throw new TimeoutException();
	}

	@Override
	public void accept(final V v)
	{
		set(v);
	}

	@Override
	public V get()
	{
		try
		{
			latch.await();
			return value.get();
		} catch (final Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning)
	{
		return false;
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@Override
	public boolean isDone()
	{
		return isAvailable();
	}

	@Override
	public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		return get(Duration.ofNanos(unit.toNanos(timeout)));
	}
}
