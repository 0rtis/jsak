package io.ortis.jsak.util.function;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception>
{
	void accept(final T t) throws E;
}
