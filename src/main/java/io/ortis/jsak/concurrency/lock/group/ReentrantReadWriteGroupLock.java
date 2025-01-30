package io.ortis.jsak.concurrency.lock.group;

import io.ortis.jsak.util.InputValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public final class ReentrantReadWriteGroupLock<T> implements GroupLock<T>
{

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Condition writeCondition = lock.writeLock().newCondition();
	private final Map<T, GroupLock<T>> unitToGroupLock = new HashMap<>();
	private final Map<Group<T>, GroupLock<T>> groupToGroupLock = new HashMap<>();

	@Override
	public LockInfo<T> getLockInfo(final Group<T> group)
	{
		this.lock.readLock().lock();
		try
		{
			final GroupLock<T> groupLock = this.groupToGroupLock.get(group);
			return groupLock == null ? LockInfo.Unlocked.of(group) : groupLock;
		} finally
		{
			this.lock.readLock().unlock();
		}
	}

	@Override
	public LockInfo<T> getLockInfo(final Group<T> group, final Duration timeout) throws InterruptedException
	{
		if (!this.lock.readLock().tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS))
			return null;
		try
		{
			final GroupLock<T> groupLock = this.groupToGroupLock.get(group);
			return groupLock == null ? LockInfo.Unlocked.of(group) : groupLock;
		} finally
		{
			this.lock.readLock().unlock();
		}
	}

	@Override
	public LockInfo<T> tryLock(final Group<T> group, final Duration timeout) throws InterruptedException
	{

		final long timeoutMillis = InputValidator.checkNonNegative(timeout.toMillis(), "timeout");
		final long start = System.currentTimeMillis();
		GroupLock<T> groupLock = null;
		if (!this.lock.writeLock().tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS))
			return null;
		try
		{
			// Check reentrancy
			final GroupLock<T> existingGroupLock = this.groupToGroupLock.get(group);
			if (existingGroupLock != null && existingGroupLock.owner == Thread.currentThread())
			{
				existingGroupLock.incrementHoldCount();
				return existingGroupLock;
			}
			whileLoop:
			while (groupLock == null)
			{
				for (final T unit : group.units())
				{
					final GroupLock<T> gl = this.unitToGroupLock.get(unit);
					if (gl != null)
					{
						final long waitTime = timeoutMillis - (System.currentTimeMillis() - start);
						if (waitTime <= 0 || !this.writeCondition.await(waitTime, TimeUnit.MILLISECONDS))
							return null;
						continue whileLoop;
					}
				}
				groupLock = new GroupLock<>(group, Thread.currentThread()).incrementHoldCount();
				for (final T unit : group.units())
				{
					final GroupLock<T> gl = this.unitToGroupLock.put(unit, groupLock);
					if (gl != null)
						throw new IllegalStateException("Illegal state");
				}
				this.groupToGroupLock.put(group, groupLock);
			}
			return groupLock;
		} finally
		{
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public LockInfo<T> lock(final Group<T> group) throws InterruptedException
	{
		this.lock.writeLock().lock();
		try
		{

			// Check reentrancy
			final GroupLock<T> existingGroupLock = this.groupToGroupLock.get(group);
			if (existingGroupLock != null && existingGroupLock.owner == Thread.currentThread())
			{
				existingGroupLock.incrementHoldCount();
				return existingGroupLock;
			}

			GroupLock<T> groupLock = null;
			whileLoop:
			while (groupLock == null)
			{
				for (final T unit : group.units())
				{
					final GroupLock<T> gl = this.unitToGroupLock.get(unit);
					if (gl != null)
					{
						this.writeCondition.await();
						continue whileLoop;
					}
				}
				groupLock = new GroupLock<>(group, Thread.currentThread()).incrementHoldCount();
				for (final T unit : group.units())
				{
					final GroupLock<T> gl = this.unitToGroupLock.put(unit, groupLock);
					if (gl != null)
						throw new IllegalStateException("Illegal state");
				}
				this.groupToGroupLock.put(group, groupLock);
			}
			return groupLock;
		} finally
		{
			this.lock.writeLock().unlock();
		}
	}

	@Override
	public void unlock(final Group<T> group)
	{
		this.lock.writeLock().lock();
		try
		{
			final GroupLock<T> groupLock = this.groupToGroupLock.get(group);
			if (groupLock == null)
				throw new IllegalArgumentException("Group is not locked");
			if (groupLock.owner != Thread.currentThread())
				throw new IllegalArgumentException("Thread owner is not the locker");
			groupLock.decrementHoldCount();
			if (groupLock.getHoldCount() < 0)
				throw new IllegalStateException("Illegal state");
			else if (groupLock.getHoldCount() == 0)
			{
				for (final T unit : groupLock.group.units())
				{
					final GroupLock<T> gl = this.unitToGroupLock.remove(unit);
					if (gl != groupLock)
						throw new IllegalStateException("Illegal state");
				}
				if (groupLock != this.groupToGroupLock.remove(group))
					throw new IllegalStateException("Illegal state");
				this.writeCondition.signalAll();
			}
		} finally
		{
			this.lock.writeLock().unlock();
		}
	}

	private static class GroupLock<U> implements LockInfo<U>
	{
		private final Group<U> group;
		private final Thread owner;
		private int holdCount = 0;
		private final Object lock = new Object();

		private GroupLock(final Group<U> group, final Thread owner)
		{
			this.group = group;
			this.owner = owner;
		}

		public GroupLock<U> incrementHoldCount()
		{
			synchronized (this.lock)
			{
				this.holdCount++;
			}
			return this;
		}

		public GroupLock<U> decrementHoldCount()
		{
			synchronized (this.lock)
			{
				if (this.holdCount <= 0)
					throw new IllegalStateException("Illegal state");
				this.holdCount--;
			}
			return this;
		}

		@Override
		public int getHoldCount()
		{
			synchronized (this.lock)
			{
				return this.holdCount;
			}
		}

		@Override
		public Group<U> getGroup()
		{
			return this.group;
		}

		@Override
		public Thread getThreadOwner()
		{
			return this.owner;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			GroupLock<?> groupLock = (GroupLock<?>) o;
			return Objects.equals(group, groupLock.group);
		}

		@Override
		public int hashCode()
		{
			return this.group.hashCode();
		}

		@Override
		public String toString()
		{
			return "GroupLock{" +
				   "group=" + group +
				   ", owner=" + owner +
				   ", holdCount=" + holdCount +
				   '}';
		}
	}
}
