package io.ortis.jsak.concurrency.lock;

import io.ortis.jsak.concurrency.Threads;
import io.ortis.jsak.concurrency.lock.group.GroupLockTest;
import io.ortis.jsak.concurrency.lock.group.ReentrantReadWriteGroupLock;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ReentrantReadWriteMapLockTest
{

	@Nested
	public class StringReentrantReadWriteMapLockTest extends AbstractReentrantReadWriteMapLockTest<String>
	{

		public StringReentrantReadWriteMapLockTest()
		{
			super(new Supplier<>()
			{
				final AtomicInteger counter = new AtomicInteger(0);

				@Override
				public String get()
				{
					return "String%s".formatted(counter.getAndIncrement());
				}
			}, Threads.buildPlainThreadFactory(GroupLockTest.StringGroupLockTest.class, true));
		}
	}


	public abstract class AbstractReentrantReadWriteMapLockTest<K>
	{
		public static final int TIMEOUT_SECONDS = 30;
		public static final Random RANDOM = new Random(42);

		protected final Supplier<K> keySupplier;
		protected final ThreadFactory threadFactory;

		public AbstractReentrantReadWriteMapLockTest(final Supplier<K> keySupplier, final ThreadFactory threadFactory)
		{
			this.keySupplier = keySupplier;
			this.threadFactory = threadFactory;

		}

		public static <K> List<K> buildKeys(final int size, final Supplier<K> keySupplier)
		{
			final List<K> keys = new LinkedList<>();
			for (int i = 0; i < size; i++)
				keys.add(keySupplier.get());
			return keys;
		}


		@Test
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testLockAcquisitionAndRelease() throws InterruptedException
		{
			final ReentrantReadWriteMapLock<K> lock = new ReentrantReadWriteMapLock<>();
			final K key = keySupplier.get();

			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));

			lock.lockRead(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(1, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			lock.unlockRead(key);
			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));

			lock.lockWrite(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertTrue(lock.isWriteLocked(key));
			lock.unlockWrite(key);
			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
		}


		@Test
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testReentrantLock() throws InterruptedException
		{

			final ReentrantReadWriteMapLock<K> lock = new ReentrantReadWriteMapLock<>();
			final K key = keySupplier.get();

			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));

			lock.lockRead(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(1, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			lock.lockRead(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(2, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			lock.lockRead(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(3, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;
			lock.unlockRead(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(2, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;
			lock.unlockRead(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(1, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;
			lock.unlockRead(key);
			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;


			lock.lockWrite(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertTrue(lock.isWriteLocked(key));
			lock.lockWrite(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertTrue(lock.isWriteLocked(key));
			lock.lockWrite(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertTrue(lock.isWriteLocked(key));
			;
			lock.unlockWrite(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertTrue(lock.isWriteLocked(key));
			;
			lock.unlockWrite(key);
			Assertions.assertEquals(1, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertTrue(lock.isWriteLocked(key));
			;
			lock.unlockWrite(key);
			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;

		}


		@Test
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testReadLockWith2Threads() throws InterruptedException
		{
			final ReentrantReadWriteMapLock<K> lock = new ReentrantReadWriteMapLock<>();
			final K key = keySupplier.get();

			final AtomicInteger sharedCounter = new AtomicInteger(0);

			final Thread thread1 = this.threadFactory.newThread(() -> incrementCounterWithReadLock(lock, key, sharedCounter));
			final Thread thread2 = this.threadFactory.newThread(() -> incrementCounterWithReadLock(lock, key, sharedCounter));

			thread1.start();
			thread2.start();

			thread1.join();
			thread2.join();

			Assertions.assertEquals(2000, sharedCounter.get(), "Shared counter should be incremented correctly by both threads");
			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;
		}

		private void incrementCounterWithReadLock(final ReentrantReadWriteMapLock<K> lock, final K key, final AtomicInteger sharedCounter)
		{
			try
			{
				for (int i = 0; i < 1000; i++)
				{

					if (RANDOM.nextBoolean())
						lock.lockRead(key);
					else
						while (!lock.tryLockRead(key, Duration.ofMillis(3)))
							;
					Assertions.assertTrue(lock.getReadLockCount(key) > 0);
					try
					{
						sharedCounter.incrementAndGet();
						if (RANDOM.nextInt(100) < 20)
						{
							Thread.sleep(RANDOM.nextInt(20));
						}
					} finally
					{
						lock.unlockRead(key);
					}
				}
			} catch (final InterruptedException e)
			{
				Assertions.fail("Thread was interrupted unexpectedly", e);
			}
		}

		@Test
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testWriteLockWith2Threads() throws InterruptedException
		{
			final ReentrantReadWriteMapLock<K> lock = new ReentrantReadWriteMapLock<>();
			final K key = keySupplier.get();

			final AtomicInteger sharedCounter = new AtomicInteger(0);

			final Thread thread1 = this.threadFactory.newThread(() -> incrementCounterWithWriteLock(lock, key, sharedCounter));
			final Thread thread2 = this.threadFactory.newThread(() -> incrementCounterWithWriteLock(lock, key, sharedCounter));

			thread1.start();
			thread2.start();

			thread1.join();
			thread2.join();

			Assertions.assertEquals(2000, sharedCounter.get(), "Shared counter should be incremented correctly by both threads");
			Assertions.assertEquals(0, lock.lockCount());
			Assertions.assertEquals(0, lock.getReadLockCount(key));
			Assertions.assertFalse(lock.isWriteLocked(key));
			;
		}

		private void incrementCounterWithWriteLock(final ReentrantReadWriteMapLock<K> lock, final K key, final AtomicInteger sharedCounter)
		{
			try
			{
				for (int i = 0; i < 1000; i++)
				{

					if (RANDOM.nextBoolean())
						lock.lockWrite(key);
					else
						while (!lock.tryLockWrite(key, Duration.ofMillis(3)))
							;
					Assertions.assertTrue(lock.isWriteLocked(key));
					try
					{
						sharedCounter.incrementAndGet();
						if (RANDOM.nextInt(100) < 20)
						{
							Thread.sleep(RANDOM.nextInt(20));
						}
					} finally
					{
						lock.unlockWrite(key);
					}
				}
			} catch (final InterruptedException e)
			{
				Assertions.fail("Thread was interrupted unexpectedly", e);
			}
		}

		@ParameterizedTest
		@ValueSource(booleans = {true, false})
		@Timeout(value = TIMEOUT_SECONDS * 5, unit = TimeUnit.SECONDS)
		void testReadLockWithExecutors(final boolean readLock) throws InterruptedException
		{

			final int keyCount = 16;
			final int threadCount = 32;
			final int runCount = 500;
			final int maxSleepMillis = 5;

			final ReentrantReadWriteMapLock<K> lock = new ReentrantReadWriteMapLock<>();
			final List<K> keys = buildKeys(keyCount, keySupplier);


			final Map<K, Integer[]> sharedCounters = new HashMap<>();
			for (final K key : keys)
				sharedCounters.put(key, new Integer[]{1});


			Assertions.assertEquals(keyCount, keys.size());
			Assertions.assertFalse(sharedCounters.isEmpty());

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
								final K key = keys.get(RANDOM.nextInt(keyCount));


								switch (RANDOM.nextInt(3))
								{
									case 0:
										if (readLock)
											lock.lockRead(key);
										else
											lock.lockWrite(key);
										break;
									case 1:
										if (readLock)
										{
											if (!lock.tryLockRead(key, Duration.ofMillis(RANDOM.nextInt(3))))
												continue;
										} else
										{
											if (!lock.tryLockWrite(key, Duration.ofMillis(RANDOM.nextInt(3))))
												continue;
										}
										break;
									case 2:
										if (readLock)
										{
											if (!lock.tryLockRead(key))
												continue;
										} else
										{
											if (!lock.tryLockWrite(key))
												continue;
										}
										break;
									default:
										Assertions.fail("Invalid random value");
										break;
								}


								Integer[] value = sharedCounters.get(key);
								if (!readLock)
									Assertions.assertEquals(1, Math.abs(value[0]), "Invalid shared counter value %s".formatted(value[0]));
								Thread.sleep(RANDOM.nextInt(maxSleepMillis));
								int reentrancyDepth = 0;

								// Simulate some work
								final int increment = value[0] == 1 ? -1 : 1;

								if (RANDOM.nextBoolean())
									reentrancyDepth += reentrantLock(readLock, lock, key, RANDOM);

								value[0] += increment;

								if (RANDOM.nextBoolean())
								{
									reentrantUnlock(readLock, lock, key, reentrancyDepth);
									reentrancyDepth = 0;
								}
								if (RANDOM.nextBoolean())
									reentrancyDepth += reentrantLock(readLock, lock, key, RANDOM);

								Thread.sleep(RANDOM.nextInt(maxSleepMillis));
								value[0] += increment;

								reentrantUnlock(readLock, lock, key, reentrancyDepth);

								if (!readLock)
									Assertions.assertEquals(1, Math.abs(value[0]), "Invalid shared counter value %s".formatted(value[0]));
								Thread.sleep(RANDOM.nextInt(maxSleepMillis));
								if (readLock)
								{
									Assertions.assertTrue(lock.getReadLockCount(key) > 0);
									lock.unlockRead(key);

								} else
								{
									Assertions.assertTrue(lock.isWriteLocked(key));
									lock.unlockWrite(key);
								}

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


			Assertions.assertEquals(0, lock.lockCount());
			for (final K key : keys)
			{
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));

			}

			Assertions.assertFalse(sharedCounters.isEmpty());
			if (!readLock)
				for (final var entry : sharedCounters.entrySet())
					Assertions.assertEquals(1, Math.abs(entry.getValue()[0]));

		}

		public int reentrantLock(final boolean read, final ReentrantReadWriteMapLock<K> lock, final K key, final Random random)
				throws InterruptedException
		{
			final int lockDepth = RANDOM.nextInt(3);
			for (int i = 0; i < lockDepth; i++)
				if (read)
					lock.lockRead(key);
				else
					lock.lockWrite(key);
			return lockDepth;
		}

		public void reentrantUnlock(final boolean read, final ReentrantReadWriteMapLock<K> lock, final K key, final int depth)
				throws InterruptedException
		{
			for (int i = 0; i < depth; i++)
				if (read)
					lock.unlockRead(key);
				else
					lock.unlockWrite(key);
		}

		@ParameterizedTest
		@ValueSource(booleans = {true, false})
		@Timeout(value = TIMEOUT_SECONDS, unit = TimeUnit.SECONDS)
		void testTryLock(final boolean readLock) throws InterruptedException
		{
			final ReentrantReadWriteMapLock<K> lock = new ReentrantReadWriteMapLock<>();
			final K key = keySupplier.get();
			if (readLock)
			{
				lock.tryLockRead(key);
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(1, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));
			} else
			{
				lock.tryLockWrite(key);
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertTrue(lock.isWriteLocked(key));
			}


			final AtomicReference<AssertionError> error = new AtomicReference<>();
			final Thread thread = this.threadFactory.newThread(() -> {

				try
				{
					if (readLock)
					{
						Assertions.assertTrue(lock.tryLockRead(key), "Thread should not acquire the lock without waiting");
						Assertions.assertTrue(lock.tryLockRead(key, Duration.ofMillis(1000)),
								"Thread should not acquire the lock when main thread holds it");
						Assertions.assertEquals(1, lock.lockCount());
						Assertions.assertEquals(3, lock.getReadLockCount(key));
						Assertions.assertFalse(lock.isWriteLocked(key));
						lock.unlockRead(key);
						Assertions.assertEquals(1, lock.lockCount());
						Assertions.assertEquals(2, lock.getReadLockCount(key));
						Assertions.assertFalse(lock.isWriteLocked(key));
						lock.unlockRead(key);
						Assertions.assertEquals(1, lock.lockCount());
						Assertions.assertEquals(1, lock.getReadLockCount(key));
						Assertions.assertFalse(lock.isWriteLocked(key));
					} else
					{
						Assertions.assertFalse(lock.tryLockWrite(key), "Thread should not acquire the lock without waiting");
						Assertions.assertFalse(lock.tryLockWrite(key, Duration.ofMillis(1000)),
								"Thread should not acquire the lock when main thread holds it");
						Assertions.assertEquals(1, lock.lockCount());
						Assertions.assertEquals(0, lock.getReadLockCount(key));
						Assertions.assertTrue(lock.isWriteLocked(key));
					}

					if (readLock)
						Assertions.assertThrows(IllegalMonitorStateException.class, () -> lock.unlockWrite(key));
					else
						Assertions.assertThrows(IllegalMonitorStateException.class, () -> lock.unlockRead(key));

				} catch (final InterruptedException e)
				{
					Assertions.fail("Thread was interrupted unexpectedly", e);
				} catch (final AssertionError e)
				{
					error.set(e);
					throw e;
				}
			});


			thread.start();
			thread.join();

			if (error.get() != null)
				throw error.get();

			if (readLock)
			{
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(1, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));
				Assertions.assertTrue(lock.tryLockRead(key));
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(2, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));
				lock.unlockRead(key);
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(1, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));
				lock.unlockRead(key);
				Assertions.assertEquals(0, lock.lockCount());
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));

			} else
			{
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertTrue(lock.isWriteLocked(key));
				Assertions.assertTrue(lock.tryLockWrite(key));
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertTrue(lock.isWriteLocked(key));
				lock.unlockWrite(key);
				Assertions.assertEquals(1, lock.lockCount());
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertTrue(lock.isWriteLocked(key));
				lock.unlockWrite(key);
				Assertions.assertEquals(0, lock.lockCount());
				Assertions.assertEquals(0, lock.getReadLockCount(key));
				Assertions.assertFalse(lock.isWriteLocked(key));
			}

		}

	}

}
