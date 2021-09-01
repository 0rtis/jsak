package io.ortis.jsak.math;

import java.math.BigInteger;

/**
 * Helper functions for integer (Z) manipulation
 */
public abstract class ZUtils
{
	public static int toInt(final long value) throws ArithmeticException
	{//TODO: test speed of both
		if(value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
			throw new ArithmeticException("Value is outside int range");

		final int i = (int) value;

		if(i != value)
			throw new ArithmeticException("Value is outside int range");

		return BigInteger.valueOf(value).intValueExact();
	}

	public static short toShort(final long value) throws ArithmeticException
	{//TODO: test speed of both
		if(value > Short.MAX_VALUE || value < Short.MIN_VALUE)
			throw new ArithmeticException("Value is outside short range");

		final short i = (short) value;

		if(i != value)
			throw new ArithmeticException("Value is outside short range");

		return BigInteger.valueOf(value).shortValueExact();
	}

	public static short toUnsignedShort(final byte b)
	{
		return (short) Byte.toUnsignedInt(b);
	}

}
