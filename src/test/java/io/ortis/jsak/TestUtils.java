package io.ortis.jsak;

import java.util.Random;

public abstract class TestUtils
{
	private static final Random DETERMINISTIC_RANDOM = new Random(42);
	private static final double TEST_RUN_FACTOR = 1;

	public static Random getDeterministicRandom()
	{
		return DETERMINISTIC_RANDOM;
	}

	public static int computeTestRuns(final int baseTestRuns)
	{
		int runs = (int) (baseTestRuns * TestUtils.TEST_RUN_FACTOR);
		if(runs <= 0)
			runs = 1;

		return runs;
	}
}
