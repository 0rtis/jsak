package io.ortis.jsak.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

	public void setValue(final T value)
	{
		try
		{
			writeLock.lock();
			this.value = value;
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
