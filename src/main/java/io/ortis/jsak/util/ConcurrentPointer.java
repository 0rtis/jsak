package io.ortis.jsak.util;

/**
 * Thread safe implementation of {@link Pointer}
 *
 * @param <T>
 */
public class ConcurrentPointer<T>
{
	private T value;
	private final Object lock = new Object();

	public ConcurrentPointer(final T value)
	{
		this.value = value;
	}

	public T getValue()
	{
		synchronized (lock)
		{
			return this.value;
		}
	}

	public void setValue(final T value)
	{
		synchronized (lock)
		{
			this.value = value;
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" + "value=" + getValue() + "}";
	}

	public static <T> ConcurrentPointer<T> of(final T value)
	{
		return new ConcurrentPointer<>(value);
	}
}
