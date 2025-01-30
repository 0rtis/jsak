package io.ortis.jsak.util.function;

@FunctionalInterface
public interface CheckedConsumer<T>
{
	void accept(final T t) throws Exception;
}
