package io.ortis.jsak.concurrency;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public final class Futures
{

	private Futures()
	{
	}

	public static <T, R> R awaitThen(final Future<T> future, final BiFunction<T, Throwable, R> function)
			throws InterruptedException, CancellationException, TimeoutException
	{
		return awaitThen(future, function, null);
	}


	public static <T, R> R awaitThen(final Future<T> future, final BiFunction<T, Throwable, R> function, final Duration futureTimeout)
			throws InterruptedException, CancellationException, TimeoutException
	{
		final Long timeoutMillis = futureTimeout == null ? null : futureTimeout.toMillis();
		try
		{
			return function.apply(timeoutMillis == null ? future.get() : future.get(timeoutMillis, TimeUnit.MILLISECONDS), null);
		} catch (final InterruptedException | CancellationException | TimeoutException e)
		{
			throw e;
		} catch (final Throwable t)
		{
			return function.apply(null, t);
		}
	}

}
