package io.ortis.jsak.collection;


import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReentrantReadWriteMap<K, V> {

	private final Map<K, V> map;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


	public ReentrantReadWriteMap(final Map<K, V> map) {
		this.map = map;
	}


	private <R> R lockReadAndCompute(final Supplier<R> call) {
		lock.readLock().lock();
		try {
			return call.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	private <R> R lockWriteAndCompute(final Supplier<R> call) {
		lock.writeLock().lock();
		try {
			return call.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public ReentrantReadWriteLock.ReadLock readLock() {
		return lock.readLock();
	}

	public ReentrantReadWriteLock.WriteLock writeLock() {
		return lock.writeLock();
	}

	public int lockAndSize() {
		return lockReadAndCompute(map::size);
	}

	public V lockAndGet(final K key) {
		return lockReadAndCompute(() -> map.get(key));
	}

	public V unsafeGet(final K key) {
		return map.get(key);
	}

	public V lockAndPut(final K key, final V value) {
		return lockWriteAndCompute(() -> map.put(key, value));
	}

	public V lockAndRemove(final K key) {
		return lockWriteAndCompute(() -> map.remove(key));
	}

	public V unsafeRemove(final K key) {
		return map.remove(key);
	}

	public V lockAndComputeIfAbsent(final K key, final Supplier<V> call) {
		return lockWriteAndCompute(() -> map.computeIfAbsent(key, k -> call.get()));
	}

	public V unsafeComputeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}


	public static <K, V> ReentrantReadWriteMap<K, V> wrap(final Map<K, V> map) {
		return new ReentrantReadWriteMap<>(map);
	}
}