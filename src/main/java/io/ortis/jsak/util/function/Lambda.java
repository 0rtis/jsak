package io.ortis.jsak.util.function;

import java.util.function.Consumer;
import java.util.function.Function;

public class Lambda
{
	public static <E extends Exception> void compute(final CheckedRunnable<E> runnable, final Consumer<Throwable> exceptionConsumer)
	{
		try
		{
			runnable.run();
		} catch (final Throwable e)
		{
			exceptionConsumer.accept(e);
		}
	}


}
