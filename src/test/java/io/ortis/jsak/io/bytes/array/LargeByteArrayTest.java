package io.ortis.jsak.io.bytes.array;

import io.ortis.jsak.TestUtils;
import io.ortis.jsak.io.IOUtils;
import org.junit.*;

import java.io.*;
import java.util.Random;

public class LargeByteArrayTest
{
	private static final int BASE_RUNS = 500_000;
	private static final int MAX_BYTE_LENGTH = 256;

	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void testIO() throws IOException
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_RUNS);
		for (int run = 0; run < runs; run++)
		{
			final byte[] data1 = new byte[run == 0 ? 0 : random.nextInt(MAX_BYTE_LENGTH)];
			random.nextBytes(data1);
			final byte[] data2 = new byte[run == 0 || run == 1 ? 0 : random.nextInt(MAX_BYTE_LENGTH)];
			random.nextBytes(data2);
			final byte[] singleData = new byte[run == 0 | run == 10 ? 0 : random.nextInt(MAX_BYTE_LENGTH)];
			random.nextBytes(singleData);

			final byte[] buffer = new byte[1 + random.nextInt(MAX_BYTE_LENGTH - 1)];
			final LargeByteArray largeByteArray = new LargeByteArray(1 + random.nextInt(MAX_BYTE_LENGTH));

			testLargeByteArray(largeByteArray, data1, data2, singleData, buffer);
		}
	}

	private static void testLargeByteArray(final LargeByteArray lba, final byte[] data1, final byte[] data2, final byte[] singleData,
			final byte[] buffer) throws IOException
	{

		final int maxDataLength = Math.max(data1.length, data2.length);
		final int totalDataLength = data1.length + data2.length;

		final InputStream inputStream = lba.asInputStream();
		final OutputStream outputStream = lba.asOutputStream();

		Assert.assertEquals(0, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(0, lba.offset());

		// check buffer write

		lba.write(data1, 0, data1.length);
		Assert.assertEquals(data1.length, lba.length());
		Assert.assertEquals(data1.length, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		lba.rewind();
		Assert.assertEquals(data1.length, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(data1.length, inputStream.available());

		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{//write data1 in baos
			IOUtils.stream(inputStream, baos, buffer);
			Assert.assertArrayEquals(data1, baos.toByteArray());
		}

		Assert.assertEquals(data1.length, lba.length());
		Assert.assertEquals(data1.length, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		lba.rewind();
		Assert.assertEquals(data1.length, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(data1.length, inputStream.available());

		try (final ByteArrayInputStream bais = new ByteArrayInputStream(data2))
		{// write data 2 in large byte array
			IOUtils.stream(bais, outputStream, buffer);
		}

		Assert.assertEquals(maxDataLength, lba.length());
		Assert.assertEquals(data2.length, lba.offset());
		Assert.assertEquals(maxDataLength - data2.length, inputStream.available());

		lba.rewind();
		Assert.assertEquals(maxDataLength, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(maxDataLength, inputStream.available());

		for (final byte b : data2)
			Assert.assertEquals(b, lba.read());

		Assert.assertEquals(maxDataLength, lba.length());
		Assert.assertEquals(data2.length, lba.offset());
		Assert.assertEquals(maxDataLength - data2.length, inputStream.available());

		lba.rewind();
		Assert.assertEquals(maxDataLength, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(maxDataLength, inputStream.available());

		lba.write(data1);
		Assert.assertEquals(maxDataLength, lba.length());
		Assert.assertEquals(data1.length, lba.offset());
		Assert.assertEquals(maxDataLength - data1.length, inputStream.available());

		lba.write(data2);
		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(totalDataLength, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		// large byte array contains data1 + data2

		lba.seek(data1.length);
		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(data1.length, lba.offset());
		Assert.assertEquals(data2.length, inputStream.available());

		for (final byte b : data2)
			Assert.assertEquals(b, lba.read());

		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(totalDataLength, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		lba.rewind();
		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(totalDataLength, inputStream.available());

		try
		{
			lba.seek(lba.length() + 1);
			Assert.fail("Should not allow seeking out of bounds");
		} catch (final IndexOutOfBoundsException ignored)
		{

		}

		lba.seek(lba.length());
		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(totalDataLength, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		for (int i = 0; i < singleData.length; i++)
		{
			final byte b = singleData[i];
			lba.write(b);
			Assert.assertEquals(totalDataLength + i + 1, lba.length());
			Assert.assertEquals(totalDataLength + i + 1, lba.offset());
			Assert.assertEquals(0, inputStream.available());

			lba.seek(totalDataLength);
			Assert.assertEquals(totalDataLength + i + 1, lba.length());
			Assert.assertEquals(totalDataLength, lba.offset());
			Assert.assertEquals(i + 1, inputStream.available());


			lba.seek(totalDataLength+i);
			Assert.assertEquals(totalDataLength + i + 1, lba.length());
			Assert.assertEquals(totalDataLength+i, lba.offset());
			Assert.assertEquals(1, inputStream.available());

			Assert.assertEquals(b, lba.read());

			Assert.assertEquals(totalDataLength + i + 1, lba.length());
			Assert.assertEquals(totalDataLength+i+1, lba.offset());
			Assert.assertEquals(0, inputStream.available());

			lba.seek(lba.length());
			Assert.assertEquals(totalDataLength + i + 1, lba.length());
			Assert.assertEquals(totalDataLength+i+1, lba.offset());
			Assert.assertEquals(0, inputStream.available());
		}

		Assert.assertEquals(totalDataLength + singleData.length, lba.length());
		Assert.assertEquals(totalDataLength + singleData.length, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		lba.resize(totalDataLength);
		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(totalDataLength, lba.offset());
		Assert.assertEquals(0, inputStream.available());

		lba.rewind();
		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(0, lba.offset());
		Assert.assertEquals(totalDataLength, inputStream.available());

		for (final byte b : data1)
			Assert.assertEquals(b, lba.read());

		for (final byte b : data2)
			Assert.assertEquals(b, inputStream.read());

		Assert.assertEquals(totalDataLength, lba.length());
		Assert.assertEquals(totalDataLength, lba.offset());
		Assert.assertEquals(0, inputStream.available());
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
