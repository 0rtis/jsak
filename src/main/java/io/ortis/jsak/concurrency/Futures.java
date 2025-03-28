package io.ortis.jsak.concurrency;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public final class Futures
{

	private Futures()
	{
	}


	public static <R> R await(final Future<R> future, final Duration timeout)
			throws InterruptedException, ExecutionException, CancellationException, TimeoutException
	{

		try
		{
			if (timeout == null)
				return future.get();
			return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (final InterruptedException | ExecutionException | CancellationException | TimeoutException e)
		{
			throw e;
		}
	}

	public static <T, R> R awaitThen(final Future<T> future, final BiFunction<T, Throwable, R> function)
			throws InterruptedException, CancellationException, TimeoutException
	{
		return awaitThen(future, function, null);
	}


	public static <T, R> R awaitThen(final Future<T> future, final BiFunction<T, Throwable, R> function, final Duration timeout)
			throws InterruptedException, CancellationException, TimeoutException
	{
		try
		{
			return function.apply(await(future, timeout), null);
		} catch (final InterruptedException | CancellationException | TimeoutException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			return function.apply(null, t);
		}
	}

}
