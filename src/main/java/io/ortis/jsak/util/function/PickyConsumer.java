package io.ortis.jsak.util.function;

@FunctionalInterface
public interface PickyConsumer<T>
{
	boolean offer(final T t);
}
