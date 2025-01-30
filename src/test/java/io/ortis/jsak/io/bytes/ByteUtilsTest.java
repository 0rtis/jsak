package io.ortis.jsak.io.bytes;


import io.ortis.jsak.TestUtils;
import io.ortis.jsak.math.UZ;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

public class ByteUtilsTest
{

	@Test
	public void testHexadecimal()
	{
		hexTest(new byte[]{0, 0, 0, 0, 0, 0, 0}, "00000000000000");
		hexTest(new byte[]{0, 0, 0, 0, 1, 42, 3}, "00000000012A03");// leading zero conversion

		for (int i = -100_000; i < 100_000; i++)
		{
			final byte[] bytes = ByteUtils.zToBytes(i, 6);
			final String hex = ByteUtils.bytesToHexadecimal(bytes);
			Assertions.assertArrayEquals(bytes, ByteUtils.hexadecimalToBytes(hex));
			final BigInteger bi = new BigInteger(hex, 16);
			final BigInteger uz = ByteUtils.bytesToUnsignedZ(bytes);
			Assertions.assertEquals(bi, uz);
		}
	}

	private void hexTest(final byte[] bytes, final String hex)
	{
		Assertions.assertArrayEquals(bytes, ByteUtils.hexadecimalToBytes(hex));
		Assertions.assertArrayEquals(ByteUtils.hexadecimalToBytes(ByteUtils.bytesToHexadecimal(ByteUtils.hexadecimalToBytes(hex))),
				ByteUtils.hexadecimalToBytes(hex));
		Assertions.assertEquals(ByteUtils.bytesToHexadecimal(ByteUtils.hexadecimalToBytes(ByteUtils.bytesToHexadecimal(bytes))),
				ByteUtils.bytesToHexadecimal(bytes));
	}

	@Test
	public void testByteArray()
	{
		final Random random = TestUtils.getDeterministicRandom();

		for (int i = 0; i < 100_000; i++)
		{
			final byte[] bytes = new byte[random.nextInt(1024)];
			random.nextBytes(bytes);

			// copy
			Assertions.assertArrayEquals(bytes, ByteUtils.copy(bytes));
			Assertions.assertArrayEquals(bytes, ByteUtils.copy(bytes, 0, bytes.length));

			{// bits
				final boolean[] bits = ByteUtils.bytesToBits(bytes);
				final String bitString = ByteUtils.bytesToBitString(bytes);
				Assertions.assertEquals(bits.length, bitString.length());
				for (int j = 0; j < bits.length; j++)
					Assertions.assertEquals(bits[j], bitString.charAt(j) == '1');

				Assertions.assertArrayEquals(bytes, ByteUtils.bitsToBytes(bits));

				if (bytes.length == 4)
				{
					final String expected = Integer.toBinaryString(ByteUtils.bytesToZ(bytes).intValueExact());
					final int diff = bitString.length() - expected.length();

					for (int j = 0; j < diff; j++)
						Assertions.assertEquals(bitString.charAt(j), '0');

					Assertions.assertEquals(expected, bitString.substring(diff));
				}
			}

			// bytes to unsigned
			final short[] ubytes = ByteUtils.bytesToUnsignedShortArray(bytes, new short[bytes.length]);
			Assertions.assertEquals(bytes.length, ubytes.length);
			for (int j = 0; j < bytes.length; j++)
			{
				final int expected = Byte.toUnsignedInt(bytes[j]);
				final int e = bytes[j] & 0xff;
				Assertions.assertEquals(expected, ubytes[j]);
				Assertions.assertEquals(expected, ByteUtils.byteToUnsignedShort(bytes[j]));
			}

			// pack
			if (bytes.length > 3)
			{
				bytes[0] = ByteUtils.BITS_0;
				bytes[1] = ByteUtils.BITS_0;
				BigInteger full = new BigInteger(bytes);
				BigInteger packed = new BigInteger(ByteUtils.packSignedZBytes(bytes, bytes.length - 1));
				Assertions.assertEquals(full, packed);

				bytes[0] = ByteUtils.BITS_1;
				bytes[1] = ByteUtils.BITS_1;
				full = new BigInteger(bytes);
				packed = new BigInteger(ByteUtils.packSignedZBytes(bytes, bytes.length - 1));
				Assertions.assertEquals(full, packed);

			}

		}

	}

	@Test
	public void testZ()
	{

		//for(int i = -1_000_000; i < 1_000_000; i++)
		for (int i = -100_000; i < 100_000; i++)
		{
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE)
				intTest(i, 1);
			else
				try
				{
					intTest(i, 1);
					Assertions.fail("Should not allow underflow");
				} catch (final Exception e)
				{

				}

			if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE)
				intTest(i, 2);
			else
				try
				{
					intTest(i, 2);
					Assertions.fail("Should not allow underflow");
				} catch (final Exception e)
				{

				}

			intTest(i, 3);
			intTest(i, 4);
			intTest(i, 7);
			intTest(i, 8);
		}


	}

/*
	@Test
	public void unsignedTest()
	{

		int value = -285;
		final boolean[] bits = ByteUtils.bytesToBits(BigInteger.valueOf(value).toByteArray());
		String bitss = ByteUtils.bytesToBitString(BigInteger.valueOf(value).toByteArray());
		BigInteger bi = ByteUtils.bytesToZ(BigInteger.valueOf(value).toByteArray());
		BigInteger ubi = ByteUtils.bytesToUnsignedZ(BigInteger.valueOf(value).toByteArray());

		byte[] bytess = ByteUtils.bitsToBytes(bits);

		System.out.println(Arrays.toString(BigInteger.valueOf(value).toByteArray()));
		System.out.println(ByteUtils.bytesToBitString(BitSet.valueOf(BigInteger.valueOf(value).toByteArray()).toByteArray()));

		System.out.println(Arrays.toString(BigInteger.valueOf(-285).toByteArray()));
		System.out.println(ByteUtils.bytesToBitString(BitSet.valueOf(BigInteger.valueOf(-285).toByteArray()).toByteArray()));
		System.out.println(Arrays.toString(ByteUtils.bitsToBytes(ByteUtils.bytesToBits(BitSet.valueOf(BigInteger.valueOf(-285).toByteArray()).toByteArray()))));
		System.out.println(Long.toBinaryString(285));
		System.out.println(new BigInteger("-11d", 16).toString(16));
		System.out.println(BigInteger.valueOf(-285).toString(16));
		System.out.println(Long.parseLong(BigInteger.valueOf(-285).toString(16), 16));


		System.out.println(Integer.toBinaryString(7));
		System.out.println(Integer.toBinaryString(-7));
		System.out.println(Long.parseLong("9", 16));
		System.out.println(Long.decode("0xf9"));
		System.out.println(Long.parseLong("f9", 16));
		System.out.println(Long.decode("0xfffffff9"));
		System.out.println(new BigInteger(Long.toHexString(-7), 16));
		System.out.println(new BigInteger("f9", 16));
		System.out.println(new BigInteger("fffffff9", 16));
		System.out.println(Arrays.toString(new BigInteger("-fffffff9", 16).toByteArray()));


		byte[] bytes = ByteUtils.zToBytes(-7, 6);
		System.out.println(Arrays.toString(bytes));
		BigInteger s = ByteUtils.bytesToZ(bytes);
		BigInteger u = ByteUtils.bytesToZ(bytes);

		System.out.println(s);
		System.out.println(u);
	}*/

	private static void intTest(final int i, final int serialLength)
	{
		final BigInteger bi = BigInteger.valueOf(i);
		final byte[] iBytes = ByteUtils.zToBytes(i, serialLength);
		final byte[] biBytes = ByteUtils.zToBytes(bi, serialLength);

		/*System.out.println(Arrays.toString(iBytes));
		System.out.println(ByteUtils.bytesToBitString(iBytes));
		boolean [] bb = ByteUtils.bytesToBits(iBytes);
		for(int j=0;j<8;j++)
			bb[j] = true;

		System.out.println(ByteUtils.bytesToZ(ByteUtils.bitsToBytes(bb)));
		*/

		Assertions.assertArrayEquals(iBytes, biBytes);

		if (i == 0)
			for (byte b : iBytes)
				if (b != 0)
					Assertions.fail("Serial of 0 should contains zeros");


		// signed
		Assertions.assertEquals(i, ByteUtils.bytesToZ(iBytes).intValueExact());
		Assertions.assertEquals(i, ByteUtils.bytesToZ(iBytes, 0, iBytes.length).intValueExact());

		// BigInteger
		Assertions.assertEquals(bi, ByteUtils.bytesToZ(biBytes));
		Assertions.assertEquals(bi, ByteUtils.bytesToZ(biBytes, 0, biBytes.length));

		if (i >= 0)
		{
			// unsigned
			Assertions.assertEquals(bi, ByteUtils.bytesToUnsignedZ(iBytes));
			Assertions.assertEquals(bi, ByteUtils.bytesToUnsignedZ(iBytes, 0, iBytes.length));


		}
	}


}
