package io.ortis.jsak;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class TestUtils
{
	private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123446789" .toCharArray();
	private static final Random RANDOM = new Random();
	private static final Random DETERMINISTIC_RANDOM = new Random(42);
	private static final double TEST_RUN_FACTOR = 1;

	public static Random getDeterministicRandom()
	{
		return DETERMINISTIC_RANDOM;
	}

	public static int computeTestRuns(final int baseTestRuns)
	{
		int runs = (int) (baseTestRuns * TestUtils.TEST_RUN_FACTOR);
		if (runs <= 0)
			runs = 1;

		return runs;
	}

	public static String randomString(final int length)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append(CHARS[RANDOM.nextInt(CHARS.length)]);

		return sb.toString();

	}

	public static Path mkdir() throws Exception
	{

		Path folder = null;
		for (int i = 0; i < 100; i++)
		{
			folder = Path.of(UUID.randomUUID() + ".test");
			if (!Files.exists(folder))
				break;
		}

		if (Files.exists(folder))
			throw new Exception("Could not find non existing folder");

		folder = Files.createDirectories(folder);
		if (!Files.exists(folder))
			throw new Exception("Could not create folder " + folder.toAbsolutePath());

		return folder;
	}

	public static void delete(final Path file) throws Exception
	{
		if (file == null || !Files.exists(file))
			return;

		if (Files.isDirectory(file))
			for (var path : Files.walk(file).collect(Collectors.toList()))
				if (!path.equals(file))
					delete(path);

		if (!Files.deleteIfExists(file))
			throw new Exception("Could not delete file " + file.toAbsolutePath());
	}

}
