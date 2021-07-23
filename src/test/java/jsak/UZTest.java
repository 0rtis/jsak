package jsak;

import jsak.io.bytes.ByteUtils;
import jsak.io.bytes.Bytes;
import org.junit.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UZTest
{
	private static final int BASE_TEST_RUNS = 1_000_000;


	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void test()
	{
		final Random random = TestUtils.getDeterministicRandom();
		final int runs = TestUtils.computeTestRuns(BASE_TEST_RUNS);
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

			Assert.assertEquals(bi, uz.toBigInteger());
			Assert.assertEquals(bi.hashCode(), uz.toBigInteger().hashCode());
			Assert.assertEquals(bi.add(BigInteger.ONE), uz.add(UZ.ONE).toBigInteger());
			Assert.assertEquals(z, uz.toBigInteger().longValueExact());
			Assert.assertEquals(z + 1, uz.add(UZ.ONE).toBigInteger().longValueExact());

			// UZ from pack()
			{
				final UZ fromPack = uz.packBytes(uz.byteLength() + 3);
				Assert.assertEquals(fromPack, uz);
				Assert.assertEquals(fromPack.hashCode(), uz.hashCode());
				Assert.assertEquals(uz.toBigInteger(), fromPack.toBigInteger());
				Assert.assertEquals(UZ.ZERO, uz.subtract(fromPack));
			}

			{// UZ from Z
				final UZ uzFromZ = new UZ(z);

				Assert.assertEquals(uz, uzFromZ);
				Assert.assertEquals(uz.hashCode(), uzFromZ.hashCode());
				Assert.assertEquals(uz.toBigInteger(), uzFromZ.toBigInteger());
				Assert.assertEquals(uz.asBytes(), uzFromZ.asBytes());
				Assert.assertArrayEquals(uz.asBytes().toByteArray(), uzFromZ.asBytes().toByteArray());

				Assert.assertEquals(UZ.ZERO, uz.subtract(uzFromZ));
			}

			{// UZ from BigInteger
				final UZ uzFromBi = new UZ(bi);
				Assert.assertEquals(uz.hashCode(), uzFromBi.hashCode());
				Assert.assertEquals(uz, uzFromBi);
				Assert.assertEquals(uz.toBigInteger(), uzFromBi.toBigInteger());
				Assert.assertEquals(uz.asBytes(), uzFromBi.asBytes());
				Assert.assertArrayEquals(uz.asBytes().toByteArray(), uzFromBi.asBytes().toByteArray());

				Assert.assertEquals(UZ.ZERO, uz.subtract(uzFromBi));
			}

			{// UZ from BigInteger bytes
				final UZ uzFromBiBytes = new UZ(Bytes.wrap(bi.toByteArray()));
				Assert.assertEquals(uz.hashCode(), uzFromBiBytes.hashCode());
				Assert.assertEquals(uz, uzFromBiBytes);
				Assert.assertEquals(uz.toBigInteger(), uzFromBiBytes.toBigInteger());

				/* Bytes might be different since UZ does not trim the leading zeros with constructor UZ(Bytes) */
				//Assert.assertEquals(uz.toBytes(), uzFromBiBytes.toBytes());
				//Assert.assertArrayEquals(uz.toBytes().toByteArray(), uzFromBiBytes.toBytes().toByteArray());

				Assert.assertEquals(UZ.ZERO, uz.subtract(uzFromBiBytes));
			}

			{// UZ from Bytes
				final UZ uzFromBytes = new UZ(uz.asBytes());
				Assert.assertEquals(uz.hashCode(), uzFromBytes.hashCode());
				Assert.assertEquals(uz, uzFromBytes);
				Assert.assertEquals(uz.toBigInteger(), uzFromBytes.toBigInteger());
				Assert.assertEquals(uz.asBytes(), uzFromBytes.asBytes());
				Assert.assertArrayEquals(uz.asBytes().toByteArray(), uzFromBytes.asBytes().toByteArray());

				Assert.assertEquals(UZ.ZERO, uz.subtract(uzFromBytes));
			}

			{// UZ from ByteUtils bytes
				final UZ uzFromZBytes = new UZ(Bytes.wrap(ByteUtils.zToBytes(z, 8)));
				Assert.assertEquals(uz.hashCode(), uzFromZBytes.hashCode());
				Assert.assertEquals(uz, uzFromZBytes);
				Assert.assertEquals(uz.toBigInteger(), uzFromZBytes.toBigInteger());

				/* Bytes might be different since UZ does not trim the leading zeros with constructor UZ(Bytes) */
				//Assert.assertEquals(uz.toBytes(), uzFromZBytes.toBytes());
				//Assert.assertArrayEquals(uz.toBytes().toByteArray(), uzFromZBytes.toBytes().toByteArray());

				Assert.assertEquals(UZ.ZERO, uz.subtract(uzFromZBytes));
			}
		}

		try
		{
			new UZ(-1);
			Assert.fail("Should not accept negative number");
		} catch(final Exception ignored)
		{
		}


		// test sorting
		Collections.shuffle(uzs, random);
		Collections.shuffle(bis, random);

		uzs.sort(null);
		bis.sort(null);

		Assert.assertEquals(uzs.size(), bis.size());

		for(int i = 0; i < bis.size(); i++)
			Assert.assertEquals(bis.get(i), uzs.get(i).toBigInteger());
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
