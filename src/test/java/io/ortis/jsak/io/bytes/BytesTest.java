package io.ortis.jsak.io.bytes;

import io.ortis.jsak.TestUtils;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public class BytesTest
{
	private static final int BASE_RUNS = 3_000_000;
	private static final int MAX_LENGTH = 256;

	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void testGetters()
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_RUNS);
		for(int run = 0; run < runs; run++)
		{
			final int length = run == 0 ? 0 : random.nextInt(MAX_LENGTH);
			final byte[] buffer = new byte[length];
			random.nextBytes(buffer);

			final Bytes bytes = Bytes.copy(buffer);

			Assert.assertEquals(length, bytes.length());
			Assert.assertArrayEquals(buffer, bytes.toByteArray());

			final Bytes bytesw = Bytes.wrap(buffer);
			Assert.assertArrayEquals(buffer, bytesw.toByteArray());

			Assert.assertEquals(bytes, bytesw);
			Assert.assertEquals(bytes.hashCode(), bytesw.hashCode());
		}
	}

	@Test
	public void testEquals() throws IOException
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_RUNS);
		for(int run = 0; run < runs; run++)
		{
			final int length = run == 0 ? 0 : random.nextInt(MAX_LENGTH);
			final byte[] buffer = new byte[length];
			random.nextBytes(buffer);

			final byte[] buffer2 = new byte[length];
			random.nextBytes(buffer2);

			final byte[] buffer3 = new byte[length + 1];
			System.arraycopy(buffer2, 0, buffer3, 0, buffer2.length);

			final Bytes base = Bytes.copy(buffer);

			{// clone
				final Bytes branch = Bytes.copy(buffer);

				Assert.assertEquals(base, branch);
				Assert.assertEquals(base.hashCode(), branch.hashCode());

				final Bytes branchw = Bytes.copy(buffer);
				Assert.assertEquals(base, branchw);
				Assert.assertEquals(base.hashCode(), branchw.hashCode());
			}

			if(!Arrays.equals(buffer, buffer2))
			{
				{// buffer2
					final Bytes branch = Bytes.copy(buffer2);

					Assert.assertNotEquals(base, branch);

					final Bytes branchw = Bytes.copy(buffer2);
					Assert.assertNotEquals(base, branchw);

					Assert.assertEquals(branch, branchw);
					Assert.assertEquals(branch.hashCode(), branchw.hashCode());
				}

			}


			{// buffer2 + 1
				final Bytes branch = Bytes.copy(buffer3);
				Assert.assertNotEquals(base, branch);

				final Bytes branch2 = Bytes.copy(buffer3);
				Assert.assertNotEquals(base, branch2);

				Assert.assertEquals(branch, branch2);
				Assert.assertEquals(branch.hashCode(), branch2.hashCode());
			}

			{// reference byte array
				final Bytes branch = Bytes.wrap(base);

				Assert.assertEquals(base, branch);
				Assert.assertEquals(base.hashCode(), branch.hashCode());

				if(base.length() > 1)
				{
					Assert.assertEquals(0, base.getPosition());
					Assert.assertEquals(0, branch.getPosition());

					base.read();

					Assert.assertEquals(1, base.getPosition());
					Assert.assertEquals(0, branch.getPosition());
					Assert.assertEquals(base, branch);
					Assert.assertEquals(base.hashCode(), branch.hashCode());

					branch.read();
					branch.read();

					Assert.assertEquals(1, base.getPosition());
					Assert.assertEquals(2, branch.getPosition());
					Assert.assertEquals(base, branch);
					Assert.assertEquals(base.hashCode(), branch.hashCode());
				}
			}
		}
	}

	@Test
	public void testRead() throws Exception
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_RUNS);
		for(int run = 0; run < runs; run++)
		{
			final int length = run == 0 ? 0 : random.nextInt(MAX_LENGTH);
			final byte[] buffer = new byte[length];
			random.nextBytes(buffer);

			final Bytes bytes = Bytes.copy(buffer);
			readBytes(random, bytes, buffer);

			final Bytes bytesw = Bytes.copy(buffer);
			readBytes(random, bytesw, buffer);
		}
	}

	private static void readBytes(final Random random, final Bytes bytes, final byte[] baseBuffer) throws IOException
	{
		{// read all 1 byte at a time
			final byte[] ioBuffer = new byte[1 + random.nextInt(MAX_LENGTH)];

			int index = 0;
			while(bytes.getPosition() < bytes.length())
			{
				Assert.assertEquals(1, bytes.read(ioBuffer, 0, 1));
				Assert.assertEquals(ioBuffer[0], baseBuffer[index++]);
			}

			Assert.assertEquals(bytes.getPosition(), bytes.length());
			Assert.assertTrue(bytes.read(ioBuffer, 0, 1) < 0);

			bytes.rewind();

			// again using single byte read()
			index = 0;
			while(bytes.getPosition() < bytes.length())
				Assert.assertEquals(bytes.read(), baseBuffer[index++]);

			Assert.assertEquals(bytes.getPosition(), bytes.length());
			Assert.assertTrue(bytes.read(ioBuffer, 0, 1) < 0);

			try
			{
				bytes.read();
				Assert.fail("Should not allow read outside array");
			} catch(final Exception ignored)
			{
			}
		}

		bytes.rewind();

		{// read all bytes at once
			final byte[] largeIOBuffer = new byte[baseBuffer.length + 10];

			if(baseBuffer.length == 0)
				Assert.assertEquals(-1, bytes.read(largeIOBuffer, 0, 1));
			else
				Assert.assertEquals(bytes.length(), bytes.read(largeIOBuffer, 0, largeIOBuffer.length));

			for(int i = 0; i < bytes.length(); i++)
				Assert.assertEquals(baseBuffer[i], largeIOBuffer[i]);


			Assert.assertEquals(bytes.getPosition(), bytes.length());
			Assert.assertTrue(bytes.read(largeIOBuffer, 0, 1) < 0);

			try
			{
				bytes.read();
				Assert.fail("Should not allow read outside array");
			} catch(final Exception ignored)
			{
			}
		}
	}

	@BeforeClass
	public static void setUpBeforeClass()
	{

	}

	@AfterClass
	public static void tearDownAfterClass()
	{

	}
}
