package io.ortis.jsak.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class FuturesTest
{

	@Test
	void testWhenDoneDelayedSuccess() throws InterruptedException, TimeoutException
	{
		final Future<String> future = createSuccessFuture("Success", 5000);

		Assertions.assertEquals("Success", Futures.whenDone(future, (s, t) -> {
			if (t != null)
				Assertions.fail();

			return s;
		}));

	}

	@Test
	void testWhenDoneDelayedFailure() throws InterruptedException, TimeoutException
	{
		final Future<String> future = createFailFuture(5000);

		Assertions.assertEquals("Failed", Futures.whenDone(future, (s, t) -> {
			if (t == null)
				Assertions.fail();
			return "Failed";
		}));
	}

	@Test
	void testWhenDoneSucceeded() throws InterruptedException, TimeoutException
	{
		final Future<String> future = createCompletedFuture("Success");

		Assertions.assertEquals("Success", Futures.whenDone(future, (s, t) -> {
			if (t != null)
				Assertions.fail();

			return s;
		}));
	}

	@Test
	void testWhenDoneFailed() throws InterruptedException, TimeoutException
	{
		final Future<String> future = createFailedFuture(new Exception("Fail"));

		Assertions.assertEquals("Failed", Futures.whenDone(future, (s, t) -> {
			if (t == null)
				Assertions.fail();
			return "Failed";
		}));
	}

	private static <T> Future<T> createCompletedFuture(final T result)
	{
		return CompletableFuture.completedFuture(result);
	}

	private static <T> Future<T> createFailedFuture(final Exception exception)
	{
		return CompletableFuture.failedFuture(exception);
	}

	private static <T> Future<T> createSuccessFuture(final T result, final long delay)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				Thread.sleep(delay);
			} catch (final InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			return result;
		});
	}

	private static <T> Future<T> createFailFuture(final long delay)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				Thread.sleep(delay);
			} catch (final InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException("Fail");
		});
	}
}
