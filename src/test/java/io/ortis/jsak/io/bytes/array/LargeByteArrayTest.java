package io.ortis.jsak.io.bytes.array;

import io.ortis.jsak.TestUtils;
import io.ortis.jsak.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Random;

public class LargeByteArrayTest
{
	private static final int BASE_RUNS = 500_000;
	private static final int MAX_BYTE_LENGTH = 256;


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

		Assertions.assertEquals(0, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(0, lba.offset());

		// check buffer write

		lba.write(data1, 0, data1.length);
		Assertions.assertEquals(data1.length, lba.length());
		Assertions.assertEquals(data1.length, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		lba.rewind();
		Assertions.assertEquals(data1.length, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(data1.length, inputStream.available());

		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{//write data1 in baos
			IOUtils.stream(inputStream, baos, buffer);
			Assertions.assertArrayEquals(data1, baos.toByteArray());
		}

		Assertions.assertEquals(data1.length, lba.length());
		Assertions.assertEquals(data1.length, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		lba.rewind();
		Assertions.assertEquals(data1.length, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(data1.length, inputStream.available());

		try (final ByteArrayInputStream bais = new ByteArrayInputStream(data2))
		{// write data 2 in large byte array
			IOUtils.stream(bais, outputStream, buffer);
		}

		Assertions.assertEquals(maxDataLength, lba.length());
		Assertions.assertEquals(data2.length, lba.offset());
		Assertions.assertEquals(maxDataLength - data2.length, inputStream.available());

		lba.rewind();
		Assertions.assertEquals(maxDataLength, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(maxDataLength, inputStream.available());

		for (final byte b : data2)
			Assertions.assertEquals(b, lba.read());

		Assertions.assertEquals(maxDataLength, lba.length());
		Assertions.assertEquals(data2.length, lba.offset());
		Assertions.assertEquals(maxDataLength - data2.length, inputStream.available());

		lba.rewind();
		Assertions.assertEquals(maxDataLength, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(maxDataLength, inputStream.available());

		lba.write(data1);
		Assertions.assertEquals(maxDataLength, lba.length());
		Assertions.assertEquals(data1.length, lba.offset());
		Assertions.assertEquals(maxDataLength - data1.length, inputStream.available());

		lba.write(data2);
		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(totalDataLength, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		// large byte array contains data1 + data2

		lba.seek(data1.length);
		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(data1.length, lba.offset());
		Assertions.assertEquals(data2.length, inputStream.available());

		for (final byte b : data2)
			Assertions.assertEquals(b, lba.read());

		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(totalDataLength, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		lba.rewind();
		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(totalDataLength, inputStream.available());

		try
		{
			lba.seek(lba.length() + 1);
			Assertions.fail("Should not allow seeking out of bounds");
		} catch (final IndexOutOfBoundsException ignored)
		{

		}

		lba.seek(lba.length());
		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(totalDataLength, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		for (int i = 0; i < singleData.length; i++)
		{
			final byte b = singleData[i];
			lba.write(b);
			Assertions.assertEquals(totalDataLength + i + 1, lba.length());
			Assertions.assertEquals(totalDataLength + i + 1, lba.offset());
			Assertions.assertEquals(0, inputStream.available());

			lba.seek(totalDataLength);
			Assertions.assertEquals(totalDataLength + i + 1, lba.length());
			Assertions.assertEquals(totalDataLength, lba.offset());
			Assertions.assertEquals(i + 1, inputStream.available());


			lba.seek(totalDataLength+i);
			Assertions.assertEquals(totalDataLength + i + 1, lba.length());
			Assertions.assertEquals(totalDataLength+i, lba.offset());
			Assertions.assertEquals(1, inputStream.available());

			Assertions.assertEquals(b, lba.read());

			Assertions.assertEquals(totalDataLength + i + 1, lba.length());
			Assertions.assertEquals(totalDataLength+i+1, lba.offset());
			Assertions.assertEquals(0, inputStream.available());

			lba.seek(lba.length());
			Assertions.assertEquals(totalDataLength + i + 1, lba.length());
			Assertions.assertEquals(totalDataLength+i+1, lba.offset());
			Assertions.assertEquals(0, inputStream.available());
		}

		Assertions.assertEquals(totalDataLength + singleData.length, lba.length());
		Assertions.assertEquals(totalDataLength + singleData.length, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		lba.resize(totalDataLength);
		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(totalDataLength, lba.offset());
		Assertions.assertEquals(0, inputStream.available());

		lba.rewind();
		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(0, lba.offset());
		Assertions.assertEquals(totalDataLength, inputStream.available());

		for (final byte b : data1)
			Assertions.assertEquals(b, lba.read());

		for (final byte b : data2)
			Assertions.assertEquals(b, inputStream.read());

		Assertions.assertEquals(totalDataLength, lba.length());
		Assertions.assertEquals(totalDataLength, lba.offset());
		Assertions.assertEquals(0, inputStream.available());
	}

}
