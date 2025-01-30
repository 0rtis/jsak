package io.ortis.jsak.concurrency.lock.group;


import io.ortis.jsak.concurrency.Threads;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


public class GroupLockTest<T>
{

	@Nested
	public class StringGroupLockTest extends AbstractGroupLockTest<String>
	{

		public StringGroupLockTest()
		{
			super(ReentrantReadWriteGroupLock::new, new Supplier<>()
			{
				final AtomicInteger counter = new AtomicInteger(0);

				@Override
				public String get()
				{
					return "String%s".formatted(counter.getAndIncrement());
				}
			}, Threads.buildPlainThreadFactory(StringGroupLockTest.class, true), true);
		}
	}


	public abstract class AbstractGroupLockTest<T>
	{
		public static final int TIMEOUT_SECONDS = 30;
		public static final Random RANDOM = new Random(42);

		protected final Supplier<GroupLock<T>> groupLockSupplier;
		protected final Supplier<T> unitSupplier;
		protected final ThreadFactory threadFactory;
		private final boolean reentrant;

		public AbstractGroupLockTest(final Supplier<GroupLock<T>> groupLockSupplier, final Supplier<T> unitSupplier,
				final ThreadFactory threadFactory,
				final boolean reentrant)
		{
			this.groupLockSupplier = groupLockSupplier;
			this.unitSupplier = unitSupplier;
			this.threadFactory = threadFactory;
			this.reentrant = reentrant;
		}

		public static <E> GroupLock.Group<E> buildGroup(final int size, final Supplier<E> unitSupplier)
		{
			final List<E> units = new LinkedList<>();
			for (int i = 0; i < size; i++)
				units.add(unitSupplier.get());
			return GroupLock.Group.<E>of(units);
		}

		@Test
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testBadParams()
		{
			Assertions.assertThrows(IllegalArgumentException.class, () -> GroupLock.Group.of(null));
			Assertions.assertThrows(IllegalArgumentException.class, () -> GroupLock.Group.of(List.of()));
		}

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 3, 7})
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testLockAcquisitionAndRelease(final int groupSize) throws InterruptedException
		{
			final GroupLock<T> lock = groupLockSupplier.get();
			final GroupLock.Group<T> group = buildGroup(groupSize, unitSupplier);
			Assertions.assertFalse(lock.isLocked(group), "Should not be locked initially");
			lock.lock(group);
			try
			{
				Assertions.assertTrue(lock.isLocked(group), "Should be locked after acquiring");
				assertLockInfo(group, Thread.currentThread(), 1, lock.getLockInfo(group));
			} finally
			{
				lock.unlock(group);
			}
			Assertions.assertFalse(lock.isLocked(group), "Should be released after unlocking");
			Assertions.assertFalse(lock.getLockInfo(group).isLocked());
		}


		@ParameterizedTest
		@ValueSource(ints = {1, 2, 3, 7})
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testReentrantLock(final int groupSize) throws InterruptedException
		{
			if (!this.reentrant)
				return;
			final GroupLock<T> lock = this.groupLockSupplier.get();
			final GroupLock.Group<T> group = buildGroup(groupSize, unitSupplier);

			Assertions.assertFalse(lock.isLocked(group), "Should not be locked initially");

			final GroupLock.LockInfo<T> lockInfo = lock.lock(group);
			Assertions.assertTrue(lock.isLocked(group), "Should be locked");
			Assertions.assertEquals(group, lockInfo.getGroup());
			Assertions.assertEquals(1, lockInfo.getHoldCount());
			Assertions.assertEquals(Thread.currentThread(), lockInfo.getThreadOwner());

			Assertions.assertEquals(lockInfo, lock.lock(group));
			Assertions.assertTrue(lock.isLocked(group), "Should be locked");
			Assertions.assertEquals(group, lockInfo.getGroup());
			Assertions.assertEquals(2, lockInfo.getHoldCount());
			Assertions.assertEquals(Thread.currentThread(), lockInfo.getThreadOwner());

			lock.unlock(group);
			Assertions.assertTrue(lock.isLocked(group), "Should be locked");
			Assertions.assertEquals(group, lockInfo.getGroup());
			Assertions.assertEquals(1, lockInfo.getHoldCount());
			Assertions.assertEquals(Thread.currentThread(), lockInfo.getThreadOwner());

			lock.unlock(group);
			Assertions.assertFalse(lock.isLocked(group), "Should be released after unlocking");
			Assertions.assertEquals(group, lockInfo.getGroup());
			Assertions.assertEquals(0, lockInfo.getHoldCount());
			Assertions.assertEquals(Thread.currentThread(), lockInfo.getThreadOwner());

		}


		@ParameterizedTest
		@ValueSource(ints = {1, 2, 3, 7})
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testLockWithSingleGroup(final int groupSize) throws InterruptedException
		{
			final GroupLock<T> lock = groupLockSupplier.get();
			final GroupLock.Group<T> group = buildGroup(groupSize, unitSupplier);

			final AtomicInteger sharedCounter = new AtomicInteger(0);

			final Thread thread1 = this.threadFactory.newThread(() -> incrementCounterWithLock(lock, group, sharedCounter));
			final Thread thread2 = this.threadFactory.newThread(() -> incrementCounterWithLock(lock, group, sharedCounter));

			thread1.start();
			thread2.start();

			thread1.join();
			thread2.join();

			Assertions.assertEquals(2000, sharedCounter.get(), "Shared counter should be incremented correctly by both threads");
		}

		private void incrementCounterWithLock(final GroupLock<T> lock, final GroupLock.Group<T> group, final AtomicInteger sharedCounter)
		{
			try
			{
				for (int i = 0; i < 1000; i++)
				{

					GroupLock.LockInfo<T> lockInfo = null;

					if (RANDOM.nextBoolean())
						lockInfo = lock.lock(group);
					else
						while (lockInfo == null)
							lockInfo = lock.tryLock(group, Duration.ofMillis(3));
					assertLockInfo(group, Thread.currentThread(), 1, lockInfo);
					try
					{
						sharedCounter.incrementAndGet();
						if (RANDOM.nextInt(100) < 20)
						{
							Thread.sleep(RANDOM.nextInt(20));
						}
					} finally
					{
						lock.unlock(group);
					}
				}
			} catch (final InterruptedException e)
			{
				Assertions.fail("Thread was interrupted unexpectedly", e);
			}
		}

		@Test
		@Timeout(value = TIMEOUT_SECONDS * 5, unit = TimeUnit.SECONDS)
		void testLockWithMultipleGroup() throws InterruptedException
		{
			final int unitCount = 100;
			final int groupCount = 25;
			final int maxGroupSize = 2 * unitCount / groupCount;
			final int threadCount = 32;
			final int runCount = 500;
			final int maxSleepMillis = 5;

			final GroupLock<T> lock = groupLockSupplier.get();
			final List<T> units = new ArrayList<>();
			for (int i = 0; i < unitCount; i++)
				units.add(unitSupplier.get());

			final List<GroupLock.Group<T>> groups = new ArrayList<>();
			final Map<GroupLock.Group<T>, Integer[]> sharedCounters = new HashMap<>();
			for (int i = 0; i < groupCount; i++)
			{
				final int size = RANDOM.nextInt(maxGroupSize) + 1;
				final Set<T> groupUnits = new HashSet<>();
				for (int j = 0; j < size; j++)
					groupUnits.add(units.get(RANDOM.nextInt(unitCount)));
				final GroupLock.Group<T> group = GroupLock.Group.of(groupUnits);
				groups.add(group);
				sharedCounters.put(group, new Integer[]{1});
			}

			Assertions.assertEquals(groupCount, groups.size());
			Assertions.assertFalse(
					sharedCounters.isEmpty());// It is possible that the same group was randomly generated multiple times but the map would still have one entry for this group.

			try (final ExecutorService executorService = Executors.newFixedThreadPool(threadCount + 1, threadFactory))
			{
				final List<Future<?>> futures = new ArrayList<>(threadCount);
				for (int i = 0; i < threadCount; i++)
				{
					futures.add(executorService.submit(() -> {
						try
						{
							for (int j = 0; j < runCount; j++)
							{
								final GroupLock.Group<T> group = groups.get(RANDOM.nextInt(groupCount));
								GroupLock.LockInfo<T> lockInfo = null;

								final GroupLock.LockInfo<T> previousLockInfo = lock.getLockInfo(group);

								if (previousLockInfo.isLocked())
									Assertions.assertNotEquals(Thread.currentThread(), previousLockInfo.getThreadOwner());

								switch (RANDOM.nextInt(3))
								{
									case 0:
										lockInfo = lock.lock(group);
										assertLockInfo(group, Thread.currentThread(), 1, lockInfo);
										break;
									case 1:
										lockInfo = lock.tryLock(group, Duration.ofMillis(RANDOM.nextInt(3)));
										if (lockInfo == null)
											continue;
										break;
									case 2:
										lockInfo = lock.tryLock(group);
										if (lockInfo == null)
											continue;
										break;
									default:
										Assertions.fail("Invalid random value");
										break;
								}


								Integer[] value = sharedCounters.get(group);
								Assertions.assertEquals(1, Math.abs(value[0]), "Invalid shared counter value %s".formatted(value[0]));
								Thread.sleep(RANDOM.nextInt(maxSleepMillis));
								int reentrancyDepth = 0;

								// Simulate some work
								final int increment = value[0] == 1 ? -1 : 1;

								if (RANDOM.nextBoolean() && this.reentrant)
									reentrancyDepth += reentrantLock(lock, group, RANDOM);

								value[0] += increment;

								if (RANDOM.nextBoolean())
								{
									reentrantUnlock(lock, group, reentrancyDepth);
									reentrancyDepth = 0;
								}
								if (RANDOM.nextBoolean() && this.reentrant)
									reentrancyDepth += reentrantLock(lock, group, RANDOM);

								Thread.sleep(RANDOM.nextInt(maxSleepMillis));
								value[0] += increment;

								reentrantUnlock(lock, group, reentrancyDepth);

								Assertions.assertEquals(1, Math.abs(value[0]), "Invalid shared counter value %s".formatted(value[0]));
								Thread.sleep(RANDOM.nextInt(maxSleepMillis));
								Assertions.assertTrue(lockInfo.isLocked());
								lock.unlock(group);
								Assertions.assertFalse(lockInfo.isLocked());
							}
						} catch (final Throwable e)
						{
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}));

				}
				executorService.shutdown();
				Assertions.assertTrue(executorService.awaitTermination(1000, TimeUnit.DAYS));

				for (final var future : futures)
					try
					{
						future.get();
					} catch (final Throwable e)
					{
						Assertions.fail(e);
					}

			}


			Assertions.assertEquals(groupCount, groups.size());
			Assertions.assertFalse(sharedCounters.isEmpty());
			for (final var entry : sharedCounters.entrySet())
				Assertions.assertEquals(1, Math.abs(entry.getValue()[0]));

		}

		public int reentrantLock(final GroupLock<T> lock, final GroupLock.Group<T> group, final Random random) throws InterruptedException
		{
			final int lockDepth = random.nextInt(3);
			final GroupLock.LockInfo<T> lockInfo = lock.getLockInfo(group);
			for (int i = 0; i < lockDepth; i++)
				Assertions.assertEquals(lockInfo, lock.lock(group));
			return lockDepth;
		}

		public void reentrantUnlock(final GroupLock<T> lock, final GroupLock.Group<T> group, final int depth) throws InterruptedException
		{
			final GroupLock.LockInfo<T> lockInfo = lock.getLockInfo(group);
			Assertions.assertEquals(depth + 1, lockInfo.getHoldCount());
			for (int i = 0; i < depth; i++)
			{
				lock.unlock(group);
				Assertions.assertEquals(depth - i, lockInfo.getHoldCount());
			}
			assertLockInfo(group, Thread.currentThread(), 1, lockInfo);
		}

		@ParameterizedTest
		@ValueSource(ints = {1, 2, 3, 7})
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testTryLock(final int groupSize) throws InterruptedException
		{
			final GroupLock<T> lock = groupLockSupplier.get();
			final GroupLock.Group<T> group = buildGroup(groupSize, unitSupplier);
			final GroupLock.LockInfo<T> lockInfo = lock.tryLock(group);
			Assertions.assertNotNull(lockInfo, "Should acquire the lock");
			Assertions.assertEquals(lockInfo, lock.getLockInfo(group));
			assertLockInfo(group, Thread.currentThread(), 1, lockInfo);
			final Thread mainThread = Thread.currentThread();

			final Thread thread = this.threadFactory.newThread(() -> {

				try
				{
					Assertions.assertNull(lock.tryLock(group), "Thread should not acquire the lock without waiting");
					Assertions.assertNull(lock.tryLock(group, Duration.ofMillis(1000)),
							"Thread should not acquire the lock when main thread holds it");
					assertLockInfo(group, mainThread, 1, lock.getLockInfo(group));
				} catch (final InterruptedException e)
				{
					Assertions.fail("Thread was interrupted unexpectedly", e);
				} finally
				{
					Assertions.assertThrows(IllegalArgumentException.class, () -> lock.unlock(group));
				}
			});

			thread.start();
			thread.join();

			Assertions.assertTrue(lock.isLocked(group));
			assertLockInfo(group, Thread.currentThread(), 1, lockInfo);
			Assertions.assertEquals(lockInfo, lock.tryLock(group));
			assertLockInfo(group, Thread.currentThread(), 2, lockInfo);
			Assertions.assertTrue(lock.isLocked(group));
			lock.unlock(group);
			Assertions.assertTrue(lock.isLocked(group));
			assertLockInfo(group, Thread.currentThread(), 1, lockInfo);
			lock.unlock(group);
			Assertions.assertFalse(lock.isLocked(group));
			Assertions.assertFalse(lockInfo.isLocked());
			Assertions.assertFalse(lock.getLockInfo(group).isLocked());

		}

		protected void assertLockInfo(final GroupLock.Group<T> expectedGroup, final Thread expectedOwner, final int expectedHoldCount,
				final GroupLock.LockInfo<T> lockInfo)
		{
			Assertions.assertEquals(expectedGroup, lockInfo.getGroup());
			Assertions.assertEquals(expectedOwner, lockInfo.getThreadOwner());
			Assertions.assertEquals(expectedHoldCount, lockInfo.getHoldCount());
		}
	}
}