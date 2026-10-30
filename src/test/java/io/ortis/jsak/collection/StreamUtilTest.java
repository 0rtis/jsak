package io.ortis.jsak.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

public class StreamUtilTest
{

	@Test
	void testConcatMultipleStreams()
	{
		final Stream<Integer> concatenated = StreamUtil.concat(
				Stream.of(1, 2),
				Stream.of(3),
				Stream.of(4, 5)
		);

		Assertions.assertEquals(List.of(1, 2, 3, 4, 5), concatenated.toList());
	}

	@Test
	void testConcatNoStream()
	{
		final Stream<Integer> concatenated = StreamUtil.concat();
		Assertions.assertTrue(concatenated.toList().isEmpty());
	}

	@Test
	void testConcatCovariantStreams()
	{
		final Stream<Number> concatenated = StreamUtil.concat(
				Stream.<Integer>of(1, 2),
				Stream.<Double>of(3.5)
		);

		Assertions.assertEquals(List.of(1, 2, 3.5), concatenated.toList());
	}
}
