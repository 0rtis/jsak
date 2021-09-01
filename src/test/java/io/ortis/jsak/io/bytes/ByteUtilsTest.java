package io.ortis.jsak.io.bytes;


import io.ortis.jsak.TestUtils;
import io.ortis.jsak.math.UZ;
import org.junit.*;

import java.math.BigInteger;
import java.util.Random;

public class ByteUtilsTest
{
	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void testHexadecimal()
	{

		for(int i = -100_000; i < 100_000; i++)
		{
			final byte[] bytes = ByteUtils.zToBytes(i, 6);
			final String hex = ByteUtils.bytesToHexadecimal(bytes);
			final BigInteger bi = new BigInteger(hex, 16);
			final BigInteger uz = ByteUtils.bytesToUnsignedZ(bytes);
			Assert.assertEquals(bi, uz);
		}
	}

	@Test
	public void testByteArray()
	{
		final Random random = TestUtils.getDeterministicRandom();

		for(int i = 0; i < 100_000; i++)
		{
			final byte[] bytes = new byte[random.nextInt(1024)];
			random.nextBytes(bytes);

			// copy
			Assert.assertArrayEquals(bytes, ByteUtils.copy(bytes));
			Assert.assertArrayEquals(bytes, ByteUtils.copy(bytes, 0, bytes.length));

			{// bits
				final boolean[] bits = ByteUtils.bytesToBits(bytes);
				final String bitString = ByteUtils.bytesToBitString(bytes);
				Assert.assertEquals(bits.length, bitString.length());
				for(int j = 0; j < bits.length; j++)
					Assert.assertEquals(bits[j], bitString.charAt(j) == '1');

				Assert.assertArrayEquals(bytes, ByteUtils.bitsToBytes(bits));

				if(bytes.length == 4)
				{
					final String expected = Integer.toBinaryString(ByteUtils.bytesToZ(bytes).intValueExact());
					final int diff = bitString.length() - expected.length();

					for(int j = 0; j < diff; j++)
						Assert.assertEquals(bitString.charAt(j), '0');

					Assert.assertEquals(expected, bitString.substring(diff));
				}
			}

			// bytes to unsigned
			final short[] ubytes = ByteUtils.bytesToUnsignedShortArray(bytes, new short[bytes.length]);
			Assert.assertEquals(bytes.length, ubytes.length);
			for(int j = 0; j < bytes.length; j++)
			{
				final int expected = Byte.toUnsignedInt(bytes[j]);
				final int e = bytes[j] & 0xff;
				Assert.assertEquals(expected, ubytes[j]);
				Assert.assertEquals(expected, ByteUtils.byteToUnsignedShort(bytes[j]));
			}

			// pack
			if(bytes.length > 3)
			{
				bytes[0] = ByteUtils.BITS_0;
				bytes[1] = ByteUtils.BITS_0;
				BigInteger full = new BigInteger(bytes);
				BigInteger packed = new BigInteger(ByteUtils.packSignedZBytes(bytes, bytes.length - 1));
				Assert.assertEquals(full, packed);

				bytes[0] = ByteUtils.BITS_1;
				bytes[1] = ByteUtils.BITS_1;
				full = new BigInteger(bytes);
				packed = new BigInteger(ByteUtils.packSignedZBytes(bytes, bytes.length - 1));
				Assert.assertEquals(full, packed);

			}

		}

	}

	@Test
	public void testZ()
	{

		//for(int i = -1_000_000; i < 1_000_000; i++)
		for(int i = -100_000; i < 100_000; i++)
		{
			if(i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE)
				intTest(i, 1);
			else
				try
				{
					intTest(i, 1);
					Assert.fail("Should not allow underflow");
				} catch(final Exception e)
				{

				}

			if(i >= Short.MIN_VALUE && i <= Short.MAX_VALUE)
				intTest(i, 2);
			else
				try
				{
					intTest(i, 2);
					Assert.fail("Should not allow underflow");
				} catch(final Exception e)
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

		Assert.assertArrayEquals(iBytes, biBytes);

		if(i == 0)
			for(byte b : iBytes)
				if(b != 0)
					Assert.fail("Serial of 0 should contains zeros");


		// signed
		Assert.assertEquals(i, ByteUtils.bytesToZ(iBytes).intValueExact());
		Assert.assertEquals(i, ByteUtils.bytesToZ(iBytes, 0, iBytes.length).intValueExact());

		// BigInteger
		Assert.assertEquals(bi, ByteUtils.bytesToZ(biBytes));
		Assert.assertEquals(bi, ByteUtils.bytesToZ(biBytes, 0, biBytes.length));

		if(i >= 0)
		{
			// unsigned
			Assert.assertEquals(bi, ByteUtils.bytesToUnsignedZ(iBytes));
			Assert.assertEquals(bi, ByteUtils.bytesToUnsignedZ(iBytes, 0, iBytes.length));

			// UZ
			final UZ uz = UZ.of(i);
			final byte[] uzBytes = ByteUtils.uzToBytes(uz, serialLength);

			final UZ uzb = new UZ(Bytes.copy(uzBytes));
			Assert.assertEquals(uz, uzb);

			Assert.assertArrayEquals(iBytes, uzBytes);
			Assert.assertEquals(uz, ByteUtils.bytesToUZ(uzBytes));
			Assert.assertEquals(uz, ByteUtils.bytesToUZ(uzBytes, 0, uzBytes.length));
		}
		else
		{
			final UZ uz = new UZ(Bytes.wrap(bi.toByteArray()));
			Assert.assertArrayEquals(bi.toByteArray(), uz.asBytes().toByteArray());
			Assert.assertEquals(uz.toBigInteger(), new BigInteger(ByteUtils.bytesToHexadecimal(bi.toByteArray()), 16));

			final byte[] uzBytes = ByteUtils.uzToBytes(uz, serialLength);
			final UZ uz2 = new UZ(Bytes.wrap(uzBytes));
			Assert.assertEquals(uz, uz2);
			Assert.assertEquals(uz.hashCode(), uz2.hashCode());
			Assert.assertEquals(serialLength, uz2.asBytes().length());
		}
	}


	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{

	}
}
