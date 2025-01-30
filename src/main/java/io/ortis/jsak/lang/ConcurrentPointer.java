package io.ortis.jsak.lang;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Thread safe implementation of {@link Pointer}
 *
 * @param <T>
 */
public class ConcurrentPointer<T>
{
	private T value;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock writeLock = readWriteLock.writeLock();
	private final Lock readLock = readWriteLock.readLock();

	public ConcurrentPointer(final T value)
	{
		this.value = value;
	}

	public T getValue()
	{
		try
		{
			readLock.lock();
			return this.value;
		} finally
		{
			readLock.unlock();
		}
	}

	public Discretionary<T> tryGetValue()
	{
		if (readLock.tryLock())
			try
			{

				return Discretionary.of(this.value);
			} finally
			{
				readLock.unlock();
			}
		return Discretionary.absent();
	}

	public Discretionary<T> tryGetValue(final long time, final TimeUnit unit) throws InterruptedException
	{
		if (readLock.tryLock(time, unit))
			try
			{
				return Discretionary.of(this.value);
			} finally
			{
				readLock.unlock();
			}
		return Discretionary.absent();
	}

	public T setValue(final T value)
	{
		writeLock.lock();
		try
		{
			final T oldValue = this.value;
			this.value = value;
			return oldValue;
		} finally
		{
			writeLock.unlock();
		}
	}

	public void setValue(final Function<T, T> function)
	{
		writeLock.lock();
		try
		{
			this.value = function.apply(this.value);
		} finally
		{
			writeLock.unlock();
		}
	}


	public boolean trySetValue(final T value)
	{
		if (writeLock.tryLock())
			try
			{
				this.value = value;
				return true;
			} finally
			{
				writeLock.unlock();
			}
		return false;
	}

	public boolean trySetValue(final T value, final long time, final TimeUnit unit) throws InterruptedException
	{
		if (writeLock.tryLock(time, unit))
			try
			{
				this.value = value;
				return true;
			} finally
			{
				writeLock.unlock();
			}
		return false;
	}

	public static <T> ConcurrentPointer<T> of(final T value)
	{
		return new ConcurrentPointer<>(value);
	}
}
