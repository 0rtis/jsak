package io.ortis.jsak.math;

import io.ortis.jsak.TestUtil;
import io.ortis.jsak.io.bytes.ByteArray;
import io.ortis.jsak.io.bytes.ByteUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UZTest
{
	private static final int BASE_TEST_RUNS = 1_000_000;

	

	@Test
	public void test()
	{
		final Random random = TestUtil.getDeterministicRandom();
		final int runs = TestUtil.computeTestRuns(BASE_TEST_RUNS);
		final List<UZ> uzs = new ArrayList<>();
		final List<BigInteger> bis = new ArrayList<>();
		for(int run = 0; run < runs; run++)
		{
			final long z = run == 0 ? 0 : (run == 1 ? 1 : Math.abs(random.nextLong()));
			final BigInteger bi = BigInteger.valueOf(z);
			bis.add(bi);

			// constructors
			final UZ uz = new UZ(z);
			uzs.add(uz);

			// UZ to BigInteger

			Assertions.assertEquals(bi, uz.toBigInteger());
			Assertions.assertEquals(bi.hashCode(), uz.toBigInteger().hashCode());
			Assertions.assertEquals(bi.add(BigInteger.ONE), uz.add(UZ.ONE).toBigInteger());
			Assertions.assertEquals(z, uz.toBigInteger().longValueExact());
			Assertions.assertEquals(z + 1, uz.add(UZ.ONE).toBigInteger().longValueExact());

			// UZ from pack()
			{
				final UZ fromPack = uz.packBytes(uz.byteLength() + 3);
				Assertions.assertEquals(fromPack, uz);
				Assertions.assertEquals(fromPack.hashCode(), uz.hashCode());
				Assertions.assertEquals(uz.toBigInteger(), fromPack.toBigInteger());
				Assertions.assertEquals(UZ.ZERO, uz.subtract(fromPack));
				Assertions.assertEquals(uz, UZ.of(uz.toBytes(uz.byteLength() + 3)));
			}

			{// UZ from Z
				final UZ uzFromZ = new UZ(z);

				Assertions.assertEquals(uz, uzFromZ);
				Assertions.assertEquals(uz.hashCode(), uzFromZ.hashCode());
				Assertions.assertEquals(uz.toBigInteger(), uzFromZ.toBigInteger());
				Assertions.assertEquals(uz.toBytes(), uzFromZ.toBytes());
				Assertions.assertArrayEquals(uz.toBytes().toArray(), uzFromZ.toBytes().toArray());
				Assertions.assertEquals(uz, UZ.of(uz.toBytes(uz.byteLength() + 3)));

				Assertions.assertEquals(UZ.ZERO, uz.subtract(uzFromZ));
			}

			{// UZ from BigInteger
				final UZ uzFromBi = new UZ(bi);
				Assertions.assertEquals(uz.hashCode(), uzFromBi.hashCode());
				Assertions.assertEquals(uz, uzFromBi);
				Assertions.assertEquals(uz.toBigInteger(), uzFromBi.toBigInteger());
				Assertions.assertEquals(uz.toBytes(), uzFromBi.toBytes());
				Assertions.assertArrayEquals(uz.toBytes().toArray(), uzFromBi.toBytes().toArray());
				Assertions.assertEquals(uz, UZ.of(uz.toBytes(uz.byteLength() + 3)));

				Assertions.assertEquals(UZ.ZERO, uz.subtract(uzFromBi));
			}

			{// UZ from BigInteger bytes
				final UZ uzFromBiBytes = new UZ(ByteArray.wrap(bi.toByteArray()));
				Assertions.assertEquals(uz.hashCode(), uzFromBiBytes.hashCode());
				Assertions.assertEquals(uz, uzFromBiBytes);
				Assertions.assertEquals(uz.toBigInteger(), uzFromBiBytes.toBigInteger());
				Assertions.assertEquals(uz, UZ.of(uz.toBytes(uz.byteLength() + 3)));

				/* Bytes might be different since UZ does not trim the leading zeros with constructor UZ(Bytes) */
				//Assertions.assertEquals(uz.toBytes(), uzFromBiBytes.toBytes());
				//Assertions.assertArrayEquals(uz.toBytes().toByteArray(), uzFromBiBytes.toBytes().toByteArray());

				Assertions.assertEquals(UZ.ZERO, uz.subtract(uzFromBiBytes));
			}

			{// UZ from Bytes
				final UZ uzFromBytes = new UZ(uz.toBytes());
				Assertions.assertEquals(uz.hashCode(), uzFromBytes.hashCode());
				Assertions.assertEquals(uz, uzFromBytes);
				Assertions.assertEquals(uz.toBigInteger(), uzFromBytes.toBigInteger());
				Assertions.assertEquals(uz.toBytes(), uzFromBytes.toBytes());
				Assertions.assertArrayEquals(uz.toBytes().toArray(), uzFromBytes.toBytes().toArray());

				Assertions.assertEquals(UZ.ZERO, uz.subtract(uzFromBytes));
			}

			{// UZ from ByteUtils bytes
				final UZ uzFromZBytes = new UZ(ByteArray.wrap(ByteUtil.unsignedZToBytes(z, 8)));
				Assertions.assertEquals(uz.hashCode(), uzFromZBytes.hashCode());
				Assertions.assertEquals(uz, uzFromZBytes);
				Assertions.assertEquals(uz.toBigInteger(), uzFromZBytes.toBigInteger());
				Assertions.assertEquals(uz, UZ.of(uz.toBytes(uz.byteLength() + 3)));

				/* Bytes might be different since UZ does not trim the leading zeros with constructor UZ(Bytes) */
				//Assertions.assertEquals(uz.toBytes(), uzFromZBytes.toBytes());
				//Assertions.assertArrayEquals(uz.toBytes().toByteArray(), uzFromZBytes.toBytes().toByteArray());

				Assertions.assertEquals(UZ.ZERO, uz.subtract(uzFromZBytes));
			}
		}

		try
		{
			new UZ(-1);
			Assertions.fail("Should not accept negative number");
		} catch(final Exception ignored)
		{
		}


		// test sorting
		Collections.shuffle(uzs, random);
		Collections.shuffle(bis, random);

		uzs.sort(null);
		bis.sort(null);

		Assertions.assertEquals(uzs.size(), bis.size());

		for(int i = 0; i < bis.size(); i++)
			Assertions.assertEquals(bis.get(i), uzs.get(i).toBigInteger());
	}


}
