package io.ortis.jsak.concurrency;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class Threads
{

	private Threads()
	{
	}

	public static void trySleep(final Duration sleep)
	{
		try
		{
			Thread.sleep(sleep);
		} catch (final InterruptedException _)
		{
			Thread.currentThread().interrupt();
		}
	}

	public static void trySleep(final long ms)
	{
		try
		{
			Thread.sleep(ms);
		} catch (final InterruptedException _)
		{
			Thread.currentThread().interrupt();
		}
	}

	public static String currentMethodName()
	{
		return methodName(1);
	}

	public static String methodName(final int callDepth)
	{
		final StackTraceElement[] stackTraceElements =
				Thread.currentThread().getStackTrace();

		final StackTraceElement element =
				(callDepth >= stackTraceElements.length - 2)
				? stackTraceElements[stackTraceElements.length - 1]
				: stackTraceElements[2 + callDepth];

		String className = element.getClassName();
		className = className.substring(className.lastIndexOf('.') + 1);

		return className + "." + element.getMethodName();
	}

	public static String currentSimpleMethodName()
	{
		return simpleMethodName(1);
	}

	public static String simpleMethodName(final int callDepth)
	{
		final StackTraceElement[] stackTraceElements =
				Thread.currentThread().getStackTrace();

		final StackTraceElement element =
				(callDepth >= stackTraceElements.length - 2)
				? stackTraceElements[stackTraceElements.length - 1]
				: stackTraceElements[2 + callDepth];

		return element.getMethodName();
	}

	public static ThreadFactory buildPlainThreadFactory(final Class<?> clazz, final boolean daemon)
	{
		return buildPlainThreadFactory(clazz.getSimpleName(), daemon);
	}

	public static ThreadFactory buildPlainThreadFactory(final String name, final boolean daemon)
	{
		return buildThreadFactory(name, daemon);
	}

	private static ThreadFactory buildThreadFactory(final String name, final boolean daemon)
	{
		return new ThreadFactory()
		{
			final AtomicInteger counter = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable runnable)
			{
				final Thread thread = new Thread(runnable, "%s-%s".formatted(name, counter.getAndIncrement()));
				thread.setDaemon(daemon);
				return thread;
			}
		};
	}

}
