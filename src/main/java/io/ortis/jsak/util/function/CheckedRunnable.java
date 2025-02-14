package io.ortis.jsak.util.function;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedRunnable<E extends Exception>
{
	void run() throws E;

}
