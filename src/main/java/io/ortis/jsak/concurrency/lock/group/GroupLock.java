package io.ortis.jsak.concurrency.lock.group;

import io.ortis.jsak.util.InputValidator;

import java.time.Duration;
import java.util.*;

public interface GroupLock<U>
{
	LockInfo<U> getLockInfo(final Group<U> group);

	LockInfo<U> getLockInfo(final Group<U> group, final Duration timeout) throws InterruptedException;

	default boolean isLocked(final Group<U> group)
	{
		final LockInfo<U> lockInfo = getLockInfo(group);
		return lockInfo != null && lockInfo.getHoldCount() > 0;
	}

	LockInfo<U> tryLock(final Group<U> group, final Duration timeout) throws InterruptedException;

	default LockInfo<U> tryLock(final Group<U> group) throws InterruptedException
	{
		return tryLock(group, Duration.ZERO);
	}

	LockInfo<U> lock(final Group<U> group) throws InterruptedException;

	void unlock(final Group<U> group);

	interface LockInfo<U>
	{
		Group<U> getGroup();

		Thread getThreadOwner();

		int getHoldCount();

		default boolean isLocked()
		{
			return getHoldCount() > 0;
		}

		public static class Unlocked<U> implements LockInfo<U>
		{
			private final Group<U> group;

			private Unlocked(final Group<U> group)
			{
				this.group = group;
			}

			@Override
			public Group<U> getGroup()
			{
				return this.group;
			}

			@Override
			public Thread getThreadOwner()
			{
				return null;
			}

			@Override
			public int getHoldCount()
			{
				return 0;
			}

			public static <U> Unlocked<U> of(final Group<U> group)
			{
				return new Unlocked<>(group);
			}
		}
	}

	final class Group<U>
	{
		private final Set<U> units;
		private transient final int hashCode;

		private Group(final Set<U> units)
		{
			this.units = units;
			InputValidator.checkStrictlyPositive(this.units.size(), "units.size");
			for (final U unit : this.units)
				if (unit == null)
					throw new IllegalArgumentException("Some units are null");
			this.hashCode = Objects.hash(this.units);
		}

		public Set<U> units()
		{
			return this.units;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Group<?> other = (Group<?>) o;
			return units.size() == other.units.size() && units.containsAll(other.units);
		}

		@Override
		public int hashCode()
		{
			return this.hashCode;
		}

		@Override
		public String toString()
		{
			return "%s".formatted(this.units);
		}

		public static <E> Group<E> of(final Collection<E> units)
		{
			return new Group<>(Set.copyOf(InputValidator.checkNotNull(units, "units")));
		}

		public static <E> Group<E> of(final E u1)
		{
			return of(Collections.singletonList(u1));
		}

		public static <E> Group<E> of(final E u1, final E u2)
		{
			return of(Arrays.asList(u1, u2));
		}

		public static <E> Group<E> of(final E u1, final E u2, final E u3)
		{
			return of(Arrays.asList(u1, u2, u3));
		}
	}
}
