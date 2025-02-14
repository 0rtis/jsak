package io.ortis.jsak.io.bytes;

import io.ortis.jsak.TestUtil;
import io.ortis.jsak.io.BytesProvider;
import io.ortis.jsak.io.IoUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public class ByteArrayTest
{
	private static final int BASE_RUNS = 3_000_000;
	private static final int MAX_LENGTH = 1024;


	@Test
	public void testGetters() throws IOException
	{
		final Random random = TestUtil.getDeterministicRandom();
		final int runs = TestUtil.computeTestRuns(BASE_RUNS);
		for (int run = 0; run < runs; run++)
		{
			final int length = run == 0 ? 0 : random.nextInt(MAX_LENGTH);
			final byte[] buffer = new byte[length];
			random.nextBytes(buffer);

			final ByteArray bytes = ByteArray.copy(buffer);
			final BytesProvider provider = bytes.newBytesProvider();

			Assertions.assertEquals(length, bytes.length());
			Assertions.assertArrayEquals(buffer, bytes.toArray());

			final ByteArray bytesw = ByteArray.wrap(buffer);
			Assertions.assertArrayEquals(buffer, bytesw.toArray());

			Assertions.assertEquals(bytes, bytesw);
			Assertions.assertEquals(bytes.hashCode(), bytesw.hashCode());

			final byte[] buffer2 = new byte[length];
			IoUtil.stream(provider, buffer2, 0, length);
			Assertions.assertArrayEquals(buffer, buffer2);
		}
	}

	@Test
	public void testEquals() throws IOException
	{
		final Random random = TestUtil.getDeterministicRandom();
		final int runs = TestUtil.computeTestRuns(BASE_RUNS);
		for (int run = 0; run < runs; run++)
		{
			final int length = run == 0 ? 0 : random.nextInt(MAX_LENGTH);
			final byte[] buffer = new byte[length];
			random.nextBytes(buffer);

			final byte[] buffer2 = new byte[length];
			random.nextBytes(buffer2);

			final byte[] buffer3 = new byte[length + 1];
			System.arraycopy(buffer2, 0, buffer3, 0, buffer2.length);

			final ByteArray base = ByteArray.copy(buffer);

			{// clone
				final ByteArray branch = ByteArray.copy(buffer);

				Assertions.assertEquals(base, branch);
				Assertions.assertEquals(base.hashCode(), branch.hashCode());

				final ByteArray branchw = ByteArray.copy(buffer);
				Assertions.assertEquals(base, branchw);
				Assertions.assertEquals(base.hashCode(), branchw.hashCode());
			}

			if (!Arrays.equals(buffer, buffer2))
			{
				{// buffer2
					final ByteArray branch = ByteArray.copy(buffer2);

					Assertions.assertNotEquals(base, branch);

					final ByteArray branchw = ByteArray.copy(buffer2);
					Assertions.assertNotEquals(base, branchw);

					Assertions.assertEquals(branch, branchw);
					Assertions.assertEquals(branch.hashCode(), branchw.hashCode());
				}

			}


			{// buffer2 + 1
				final ByteArray branch = ByteArray.copy(buffer3);
				Assertions.assertNotEquals(base, branch);

				final ByteArray branch2 = ByteArray.copy(buffer3);
				Assertions.assertNotEquals(base, branch2);

				Assertions.assertEquals(branch, branch2);
				Assertions.assertEquals(branch.hashCode(), branch2.hashCode());
			}

			{// reference byte array
				final ByteArray branch = ByteArray.copy(base);

				Assertions.assertEquals(base, branch);
				Assertions.assertEquals(base.hashCode(), branch.hashCode());


			}
		}
	}


}
