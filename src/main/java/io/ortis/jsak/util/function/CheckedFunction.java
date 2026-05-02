package io.ortis.jsak.util.function;

@FunctionalInterface
public interface CheckedFunction<I, O, E extends Exception>
{
	O apply(final I i) throws E;

}
