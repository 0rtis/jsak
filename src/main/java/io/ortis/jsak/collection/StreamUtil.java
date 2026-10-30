package io.ortis.jsak.collection;

import java.util.stream.Stream;

public abstract class StreamUtil
{

	public static <T> Stream<T> concat(final Stream<? extends T>... streams)
	{
		return Stream.of(streams).flatMap(s -> s);
	}
}
