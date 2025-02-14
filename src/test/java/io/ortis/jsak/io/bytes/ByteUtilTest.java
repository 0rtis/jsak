package io.ortis.jsak.io.bytes;


import io.ortis.jsak.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

public class ByteUtilTest
{
	private static final int RUNS = 2_000_000;

	@Test
	public void testHexadecimal()
	{


		{
			final String hex =
					"0000000000000000000000000000000000000000000000000000000000000004000000000000000000000000f994808674cc05ea2799c6ea30d4f0782716438a0000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000006d2ed0000000000000000000000000000000000000000000000008ac7230489e80000000000000000000000000000f994808674cc05ea2799c6ea30d4f0782716438a000000000000000000000000000000000000000000000000000000000006d2ed0000000000000000000000000000000000000000000000000000000000001178000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000022f0000000000000000000000000000000000000000000000001158e460913d000000000000000000000000000000000000000000000000000000000000000000001";
			final byte[] bytes = ByteUtil.hexToBytes(hex);
			Assertions.assertEquals(hex, ByteUtil.bytesToHex(bytes));
		}

		Assertions.assertEquals("00".repeat(32), ByteUtil.zToHex(BigInteger.ZERO, 32));
		Assertions.assertEquals("00".repeat(31) + "01", ByteUtil.zToHex(BigInteger.ONE, 32));
		Assertions.assertEquals("00" + "ff".repeat(32), ByteUtil.zToHex(BigInteger.TWO.pow(256).subtract(BigInteger.ONE), 33));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ByteUtil.zToHex(BigInteger.TWO.pow(256), 32));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ByteUtil.hexToBytes("00ffg0"));

		hexTest(new byte[]{0, 0, 0, 0, 0, 0, 0}, "00000000000000");
		hexTest(new byte[]{0, 0, 0, 0, 1, 42, 3}, "00000000012A03");// leading zero conversion
		hexTest(new byte[]{0, 0, 0, 0, 1, 42, 3}, "00000000012a03");// leading zero conversion

		for (int i = -100_000; i < 100_000; i++)
		{

			{//signed
				final byte[] bytes = ByteUtil.signedZToBytes(i, 6);
				final String hex = ByteUtil.bytesToHex(bytes);
				Assertions.assertArrayEquals(bytes, ByteUtil.hexToBytes(hex));
				final byte[] buffer = new byte[bytes.length];
				ByteUtil.hexToBytes(hex, buffer, 0);
				Assertions.assertArrayEquals(bytes, buffer);
				final BigInteger uz = ByteUtil.bytesToSignedZ(bytes);
				Assertions.assertEquals(i, uz.intValueExact());
			}

			if (i >= 0)
			{//unsigned
				final byte[] bytes = ByteUtil.unsignedZToBytes(i, 6);
				final String hex = ByteUtil.bytesToHex(bytes);
				Assertions.assertArrayEquals(bytes, ByteUtil.hexToBytes(hex));
				final byte[] buffer = new byte[bytes.length];
				ByteUtil.hexToBytes(hex, buffer, 0);
				Assertions.assertArrayEquals(bytes, buffer);
				final BigInteger uz = ByteUtil.bytesToUnsignedZ(bytes);
				Assertions.assertEquals(i, uz.intValueExact());
			}
		}
	}

	private void hexTest(final byte[] bytes, final String hex)
	{
		Assertions.assertArrayEquals(bytes, ByteUtil.hexToBytes(hex));
		final byte[] buffer = new byte[bytes.length];
		ByteUtil.hexToBytes(hex, buffer, 0);
		Assertions.assertArrayEquals(bytes, buffer);
		Assertions.assertArrayEquals(ByteUtil.hexToBytes(ByteUtil.bytesToHex(ByteUtil.hexToBytes(hex))),
				ByteUtil.hexToBytes(hex));
		Assertions.assertEquals(ByteUtil.bytesToHex(ByteUtil.hexToBytes(ByteUtil.bytesToHex(bytes))),
				ByteUtil.bytesToHex(bytes));
	}

	@Test
	public void testByteArray()
	{
		final Random random = TestUtil.getDeterministicRandom();

		for (int i = 0; i < 100_000; i++)
		{
			final byte[] bytes = new byte[random.nextInt(1024)];
			random.nextBytes(bytes);

			// copy
			Assertions.assertArrayEquals(bytes, ByteUtil.copy(bytes));
			Assertions.assertArrayEquals(bytes, ByteUtil.copy(bytes, 0, bytes.length));

			{// bits
				final boolean[] bits = ByteUtil.bytesToBits(bytes);
				final String bitString = ByteUtil.bytesToBitString(bytes);
				Assertions.assertEquals(bits.length, bitString.length());
				for (int j = 0; j < bits.length; j++)
					Assertions.assertEquals(bits[j], bitString.charAt(j) == '1');

				Assertions.assertArrayEquals(bytes, ByteUtil.bitsToBytes(bits));

				if (bytes.length == 4)
				{
					final String expected = Integer.toBinaryString(ByteUtil.bytesToSignedZ(bytes).intValueExact());
					final int diff = bitString.length() - expected.length();

					for (int j = 0; j < diff; j++)
						Assertions.assertEquals(bitString.charAt(j), '0');

					Assertions.assertEquals(expected, bitString.substring(diff));
				}
			}

			// bytes to unsigned
			final short[] ubytes = ByteUtil.bytesToUnsignedShortArray(bytes, new short[bytes.length]);
			Assertions.assertEquals(bytes.length, ubytes.length);
			for (int j = 0; j < bytes.length; j++)
			{
				final int expected = Byte.toUnsignedInt(bytes[j]);
				final int e = bytes[j] & 0xff;
				Assertions.assertEquals(expected, ubytes[j]);
				Assertions.assertEquals(expected, ByteUtil.byteToUnsignedShort(bytes[j]));
			}

			// pack
			if (bytes.length > 3)
			{
				bytes[0] = ByteUtil.BITS_0;
				bytes[1] = ByteUtil.BITS_0;
				BigInteger full = new BigInteger(bytes);
				BigInteger packed = new BigInteger(ByteUtil.packSignedZBytes(bytes, bytes.length - 1));
				Assertions.assertEquals(full, packed);

				bytes[0] = ByteUtil.BITS_1;
				bytes[1] = ByteUtil.BITS_1;
				full = new BigInteger(bytes);
				packed = new BigInteger(ByteUtil.packSignedZBytes(bytes, bytes.length - 1));
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
			final int finalI = i;
			if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE)
				intTest(i, 1);
			else
				Assertions.assertThrows(IndexOutOfBoundsException.class, () -> intTest(finalI, 1), "" + i);

			if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE)
				intTest(i, 2);
			else
				Assertions.assertThrows(IndexOutOfBoundsException.class, () -> intTest(finalI, 2));

			intTest(i, 3);
			intTest(i, 4);
			intTest(i, 7);
			intTest(i, 8);
		}


	}


	@Test
	void testUnsignedZ()
	{
		final int byteLength = 32;
		final int bitRange = byteLength << 2;

		for (final BigInteger z : List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN, BigInteger.TWO.pow(bitRange).subtract(BigInteger.ONE),
				BigInteger.TWO.pow(bitRange)))
		{
			final String expectedHex = ByteUtil.zToHex(z);
			final byte[] expectedBytes = ByteUtil.unsignedZToBytes(z, byteLength);

			Assertions.assertEquals(expectedHex, ByteUtil.zToHex(z));
			Assertions.assertArrayEquals(expectedBytes, ByteUtil.unsignedZToBytes(z, byteLength));
			Assertions.assertEquals(z, ByteUtil.bytesToUnsignedZ(expectedBytes));
			Assertions.assertEquals(z, ByteUtil.hexToUnsignedZ(expectedHex));
			Assertions.assertEquals(z, ByteUtil.hexToUnsignedZ(expectedHex.toUpperCase()));
			Assertions.assertEquals(z, ByteUtil.hexToUnsignedZ(expectedHex.toLowerCase()));

		}


		for (int i = 0; i < RUNS; i++)
		{
			final BigInteger z = TestUtil.randomBigInteger(0, Long.MAX_VALUE);
			final String expectedHex = ByteUtil.zToHex(z);
			final byte[] expectedBytes = ByteUtil.unsignedZToBytes(z, byteLength);

			Assertions.assertEquals(expectedHex, ByteUtil.zToHex(z));
			Assertions.assertArrayEquals(expectedBytes, ByteUtil.unsignedZToBytes(z, byteLength));
			Assertions.assertEquals(z, ByteUtil.bytesToUnsignedZ(expectedBytes));
			Assertions.assertEquals(z, ByteUtil.hexToUnsignedZ(expectedHex));
			Assertions.assertEquals(z, ByteUtil.hexToUnsignedZ(expectedHex.toUpperCase()));
			Assertions.assertEquals(z, ByteUtil.hexToUnsignedZ(expectedHex.toLowerCase()));

		}

	}


	@Test
	void testSignedZ()
	{
		final int byteLength = 32;
		final int bitRange = byteLength << 2;
		for (final BigInteger z : List.of(BigInteger.TWO.pow(bitRange).negate(), BigInteger.TWO.pow(bitRange).subtract(BigInteger.ONE).negate(),
				BigInteger.TEN.negate(), BigInteger.ONE.negate(), BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN,
				BigInteger.TWO.pow(bitRange).subtract(BigInteger.ONE)))
		{
			final String expectedHex = ByteUtil.zToHex(z);
			final byte[] expectedBytes = ByteUtil.signedZToBytes(z, byteLength);

			Assertions.assertEquals(expectedHex, ByteUtil.zToHex(z));
			Assertions.assertArrayEquals(expectedBytes, ByteUtil.signedZToBytes(z, byteLength));
			Assertions.assertEquals(z, ByteUtil.bytesToSignedZ(expectedBytes));
			Assertions.assertEquals(z, ByteUtil.hexToSignedZ(expectedHex));
			Assertions.assertEquals(z, ByteUtil.hexToSignedZ(expectedHex.toUpperCase()));
			Assertions.assertEquals(z, ByteUtil.hexToSignedZ(expectedHex.toLowerCase()));
		}

		for (int i = 0; i < RUNS; i++)
		{
			final BigInteger z = TestUtil.randomBigInteger(Long.MIN_VALUE, Long.MAX_VALUE);
			final String expectedHex = ByteUtil.zToHex(z);
			final byte[] expectedBytes = ByteUtil.signedZToBytes(z, byteLength);

			Assertions.assertEquals(expectedHex, ByteUtil.zToHex(z));
			Assertions.assertArrayEquals(expectedBytes, ByteUtil.signedZToBytes(z, byteLength));
			Assertions.assertEquals(z, ByteUtil.bytesToSignedZ(expectedBytes));
			Assertions.assertEquals(z, ByteUtil.hexToSignedZ(expectedHex));
			Assertions.assertEquals(z, ByteUtil.hexToSignedZ(expectedHex.toUpperCase()));
			Assertions.assertEquals(z, ByteUtil.hexToSignedZ(expectedHex.toLowerCase()));

		}

	}

	private static void intTest(final int i, final int serialLength)
	{
		final BigInteger bi = BigInteger.valueOf(i);
		final byte[] iBytes = ByteUtil.signedZToBytes(i, serialLength);
		final byte[] biBytes = ByteUtil.signedZToBytes(bi, serialLength);

		Assertions.assertArrayEquals(iBytes, biBytes);

		if (i == 0)
			for (byte b : iBytes)
				if (b != 0)
					Assertions.fail("Serial of 0 should contains zeros");


		{
			// signed
			Assertions.assertEquals(i, ByteUtil.bytesToSignedZ(iBytes).intValueExact());
			Assertions.assertEquals(i, ByteUtil.bytesToSignedZ(iBytes, 0, iBytes.length).intValueExact());
			// BigInteger
			Assertions.assertEquals(bi, ByteUtil.bytesToSignedZ(biBytes));
			Assertions.assertEquals(bi, ByteUtil.bytesToSignedZ(biBytes, 0, biBytes.length));
		}

		if (i >= 0)
		{
			final byte[] uiBytes = ByteUtil.signedZToBytes(i, serialLength);
			final byte[] ubiBytes = ByteUtil.signedZToBytes(bi, serialLength);

			// unsigned
			Assertions.assertEquals(i, ByteUtil.bytesToUnsignedZ(uiBytes).intValueExact());
			Assertions.assertEquals(i, ByteUtil.bytesToUnsignedZ(uiBytes, 0, uiBytes.length).intValueExact());
			// BigInteger
			Assertions.assertEquals(bi, ByteUtil.bytesToUnsignedZ(ubiBytes));
			Assertions.assertEquals(bi, ByteUtil.bytesToUnsignedZ(ubiBytes, 0, ubiBytes.length));

		}
	}


}
