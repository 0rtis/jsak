package io.ortis.jsak.lang;

public class Exceptions
{

	public static <T extends Throwable> Discretionary<T> find(final Throwable throwable, final Class<T> clazz, final int maxDepth)
	{
		if (clazz.isInstance(throwable))
			return Discretionary.of(clazz.cast(throwable));

		Throwable current = throwable;
		for (int i = 0; i <= maxDepth && current != null; i++)
		{
			if (clazz.isInstance(current))
				return Discretionary.of(clazz.cast(current));
			current = current.getCause();
		}

		return Discretionary.absent();
	}

}
