package io.ortis.jsak.io;

import io.ortis.jsak.TestUtils;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

public class IOUtilsTest
{
	private static final int BASE_TEST_RUNS = 2_000_000;
	private static final int MAX_BYTES_LENGTH = 256;

	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void testStream() throws Exception
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_TEST_RUNS);
		for (int run = 0; run < runs; run++)
		{
			final byte[] data = new byte[run % 20 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH)];
			random.nextBytes(data);

			try (final ByteArrayInputStream source = new ByteArrayInputStream(data))
			{
				{// destination byte array
					final byte[] destination = new byte[run % 40 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH)];
					final int from = destination.length == 0 ? 0 : random.nextInt(destination.length);
					final int read = IOUtils.stream(source, destination, from, destination.length - from);


					for (int i = 0; i < from; i++)
						Assert.assertEquals(0, destination[i]);


					for (int i = 0; i < read; i++)
						Assert.assertEquals(data[i], destination[from + i]);

					for (int i = from + read; read >= 0 && i < destination.length; i++)
						Assert.assertEquals(0, destination[i]);
				}

				source.reset();

				{// destination byte stream
					try (final ByteArrayOutputStream destination = new ByteArrayOutputStream())
					{
						final byte[] buffer = new byte[run % 40 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH)];
						final int from = buffer.length == 0 ? 0 : random.nextInt(buffer.length);
						final int read = IOUtils.stream(source, destination, buffer, from, buffer.length - from);

						for (int i = 0; i < from; i++)
							Assert.assertEquals(0, buffer[i]);

						if (buffer.length - from > 0)
						{

							if (data.length > 0)
								Assert.assertEquals(read, data.length);
							else
								Assert.assertEquals(-1, read);
							Assert.assertArrayEquals(data, destination.toByteArray());
						}

						for (int i = from + read; read >= 0 && i < buffer.length; i++)
							Assert.assertEquals(0, buffer[i]);
					}
				}
			}
		}
	}

	@Test
	public void testStreamExact() throws Exception
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_TEST_RUNS);

		for (int run = 0; run < runs; run++)
		{
			final byte[] data = new byte[run % 20 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH)];
			random.nextBytes(data);

			try (final ByteArrayInputStream source = new ByteArrayInputStream(data))
			{
				final int length = run % 80 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH);

				{// destination byte array

					final byte[] destination = new byte[run % 40 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH)];

					{// indexed from
						final int from = destination.length == 0 ? 0 : random.nextInt(destination.length);

						if (length > (destination.length - from))// not enough space in destination
							try
							{
								IOUtils.streamExact(length, source, destination, from);
								Assert.fail("Should not allow destination too small for target read");
							} catch (final Exception ignored)
							{
							}
						else if (length > data.length)// read more than available
							try
							{
								IOUtils.streamExact(length, source, destination, from);
								Assert.fail("Should not allow to read more than available");
							} catch (final Exception ignored)
							{
							}
						else
						{
							IOUtils.streamExact(length, source, destination, from);

							for (int i = 0; i < from; i++)
								Assert.assertEquals(0, destination[i]);

							for (int i = 0; i < length; i++)
								Assert.assertEquals(data[i], destination[from + i]);

							for (int i = from + length; i < destination.length; i++)
								Assert.assertEquals(0, destination[i]);
						}
					}

					source.reset();
					Arrays.fill(destination, (byte) 0);

					{
						if (length > destination.length)
							try
							{
								IOUtils.streamExact(length, source, destination);
								Assert.fail("Should not allow destination too small for target read");
							} catch (final Exception ignored)
							{
							}
						else if (length > data.length)// read more than available
							try
							{
								IOUtils.streamExact(length, source, destination);
								Assert.fail("Should not allow to read more than available");
							} catch (final Exception ignored)
							{
							}
						else
						{
							IOUtils.streamExact(length, source, destination);

							for (int i = 0; i < length; i++)
								Assert.assertEquals(data[i], destination[i]);

							for (int i = length; i < destination.length; i++)
								Assert.assertEquals(0, destination[i]);
						}
					}

				}


				source.reset();

				{// destination stream

					try (final ByteArrayOutputStream destination = new ByteArrayOutputStream())
					{
						final byte[] buffer = new byte[run % 100 == 0 ? 0 : random.nextInt(MAX_BYTES_LENGTH)];
						final int bufferFrom = buffer.length == 0 ? 0 : random.nextInt(buffer.length);
						final int bufferLength = buffer.length - bufferFrom;

						if (bufferLength <= 0)// read more than available
							try
							{
								IOUtils.streamExact(length, source, destination, buffer, bufferFrom, bufferLength);
								Assert.fail("Should not allow buffer length below 1");
							} catch (final Exception ignored)
							{
							}
						else if (length > data.length)// read more than available
							try
							{
								IOUtils.streamExact(length, source, destination, buffer, bufferFrom, bufferLength);
								Assert.fail("Should not allow to read more than available");
							} catch (final Exception ignored)
							{
							}
						else
						{
							IOUtils.streamExact(length, source, destination, buffer, bufferFrom, bufferLength);
							final byte[] destinationb = destination.toByteArray();

							Assert.assertEquals(length, destinationb.length);

							for (int i = 0; i < length; i++)
								Assert.assertEquals(data[i], destinationb[i]);
						}

						source.reset();
						destination.reset();

						if (buffer.length <= 0)// read more than available
							try
							{
								IOUtils.streamExact(length, source, destination, buffer);
								Assert.fail("Should not allow buffer length below 1");
							} catch (final Exception ignored)
							{
							}
						else if (length > data.length)// read more than available
							try
							{
								IOUtils.streamExact(length, source, destination, buffer);
								Assert.fail("Should not allow to read more than available");
							} catch (final Exception ignored)
							{
							}
						else
						{
							IOUtils.streamExact(length, source, destination, buffer);
							final byte[] destinationb = destination.toByteArray();

							Assert.assertEquals(length, destinationb.length);

							for (int i = 0; i < length; i++)
								Assert.assertEquals(data[i], destinationb[i]);
						}
					}
				}
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
