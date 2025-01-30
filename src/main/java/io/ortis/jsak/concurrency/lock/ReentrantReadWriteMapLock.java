package io.ortis.jsak.concurrency.lock;

import io.ortis.jsak.collection.ReentrantReadWriteMap;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A lock that uses an internal map for concurrent access. The keys are removed from memory when they are not needed anymore.
 * @param <T>
 */
public class ReentrantReadWriteMapLock<T> {

	private final ReentrantReadWriteMap<T, Lock> locks = ReentrantReadWriteMap.wrap(new HashMap<>());

	private <R> R W(final Supplier<R> call) {
		locks.writeLock().lock();
		try {
			return call.get();
		} finally {
			locks.writeLock().unlock();
		}
	}

	public int lockCount() {
		return locks.lockAndSize();
	}

	public int getReadLockCount(final T key) {
		final Lock lock = locks.lockAndGet(key);
		return lock == null ? 0 : lock.lock.getReadLockCount();
	}

	public void lockRead(final T key) {

		W(() ->
		{
			final Lock l = locks.unsafeComputeIfAbsent(key, _ -> new Lock());
			l.count.incrementAndGet();
			return l;
		}).lock.readLock().lock();
	}

	public boolean tryLockRead(final T key) {
		final Lock lock = W(() ->
		{
			final Lock l = locks.unsafeComputeIfAbsent(key, _ -> new Lock());
			l.count.incrementAndGet();
			return l;
		});

		if (!lock.lock.readLock().tryLock()) {
			W(() -> {
				if (lock.count.decrementAndGet() <= 0)
					locks.unsafeRemove(key);
				return null;
			});
			return false;
		}
		return true;
	}

	public boolean tryLockRead(final T key, final Duration timeout) throws InterruptedException {
		final Lock lock = W(() -> {
			final Lock l = locks.unsafeComputeIfAbsent(key, _ -> new Lock());
			l.count.incrementAndGet();
			return l;
		});

		if (!lock.lock.readLock().tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			W(() -> {
				if (lock.count.decrementAndGet() <= 0)
					locks.unsafeRemove(key);
				return null;
			});
			return false;
		}
		return true;
	}

	public boolean isWriteLocked(final T key) {
		final Lock lock = locks.lockAndGet(key);
		return lock != null && lock.lock.isWriteLocked();
	}

	public void lockWrite(final T key) {
		W(() -> {
			final Lock l = locks.unsafeComputeIfAbsent(key, _ -> new Lock());
			l.count.incrementAndGet();
			return l;
		}).lock.writeLock().lock();
	}

	public boolean tryLockWrite(final T key) {
		final Lock lock = W(() -> {
			final Lock l = locks.unsafeComputeIfAbsent(key, _ -> new Lock());
			l.count.incrementAndGet();
			return l;
		});


		if (!lock.lock.writeLock().tryLock()) {
			W(() -> {
				if (lock.count.decrementAndGet() <= 0)
					locks.unsafeRemove(key);
				return null;
			});

			return false;
		}
		return true;
	}

	public boolean tryLockWrite(final T key, final Duration timeout) throws InterruptedException {
		final Lock lock = W(() -> {
			final Lock l = locks.unsafeComputeIfAbsent(key, _ -> new Lock());
			l.count.incrementAndGet();
			return l;
		});

		if (!lock.lock.writeLock().tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			W(() -> {
				if (lock.count.decrementAndGet() <= 0)
					locks.unsafeRemove(key);
				return null;
			});
			return false;
		}
		return true;
	}

	public void unlockRead(final T key) {
		W(() -> {
			final Lock lock = locks.unsafeGet(key);
			if (lock == null)
				throw new RuntimeException("Key not locked");
			lock.lock.readLock().unlock(); // insure unlock is successful
			if (lock.count.decrementAndGet() <= 0)
				locks.unsafeRemove(key);
			return null;
		});
	}

	public void unlockWrite(final T key) {
		W(() -> {
			final Lock lock = locks.unsafeGet(key);
			if (lock == null)
				throw new RuntimeException("Key not locked");
			lock.lock.writeLock().unlock();// insure unlock is successful
			if (lock.count.decrementAndGet() <= 0)
				locks.unsafeRemove(key);
			return null;
		});
	}

	private static class Lock {
		private final ReentrantReadWriteLock lock;
		private final AtomicInteger count;

		public Lock() {
			this.lock = new ReentrantReadWriteLock();
			count = new AtomicInteger(0);
		}
	}

}
