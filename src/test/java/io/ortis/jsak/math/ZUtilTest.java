package io.ortis.jsak.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class ZUtilTest
{

	@Test
	void testRoundUp()
	{
		final BigInteger bi = new BigInteger("777");
		Assertions.assertEquals(new BigInteger("780"), ZUtil.roundUp(bi, new BigInteger("10")));
		Assertions.assertEquals(new BigInteger("800"), ZUtil.roundUp(bi, new BigInteger("100")));
	}

	@Test
	void testRoundDown()
	{
		final BigInteger bi = new BigInteger("777");
		Assertions.assertEquals(new BigInteger("770"), ZUtil.roundDown(bi, new BigInteger("10")));
		Assertions.assertEquals(new BigInteger("700"), ZUtil.roundDown(bi,new BigInteger("100")));
	}
}
