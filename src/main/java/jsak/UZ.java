package jsak;

import jsak.io.bytes.ByteUtils;
import jsak.io.bytes.Bytes;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;

/**
 * Unsigned integer (as in Z+ math)
 */
public class UZ implements Comparable<UZ>
{

	public static final DecimalFormat FORMATTER;

	static
	{
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
		symbols.setGroupingSeparator('_');

		FORMATTER = new DecimalFormat("###,###.##", symbols);
	}

	public static final Comparator<UZ> COMPARATOR = Comparator.comparing(UZ::toBigInteger);

	public static final UZ ZERO = new UZ(0);
	public static final UZ ONE = new UZ(1);

	private final Bytes bytes;
	private final BigInteger uz;

	private transient final int hashCode;

	public UZ(final long z)
	{
		this(BigInteger.valueOf(z));
	}

	public UZ(final BigInteger z)
	{
		if(z.signum() < 0)
			throw new IllegalArgumentException("Z is negative");

		this.uz = z;

		final byte[] buffer = this.uz.toByteArray();
		final int fillingZeros = fillingZeros(buffer, 0, buffer.length);
		this.bytes = Bytes.copy(buffer, fillingZeros, buffer.length - fillingZeros);

		this.hashCode = this.uz.hashCode();
	}

	/**
	 * {@link Bytes} parameter is kept as it is. Thus, identical value of UZ might have different return of {@link UZ#asBytes}
	 *
	 * @param bytes
	 */
	public UZ(final Bytes bytes)
	{
		final byte[] copy = bytes.toByteArray();
		if(copy.length <= 0)
			throw new IllegalArgumentException("Bytes is empty");

		this.bytes = Bytes.wrap(copy);
		this.uz = ByteUtils.bytesToUnsignedZ(copy);

		this.hashCode = this.uz.hashCode();
	}

	/**
	 * uz + 1
	 *
	 * @return
	 */
	public UZ pp()
	{
		return add(UZ.ONE);
	}

	/**
	 * uz - 1
	 *
	 * @return
	 */
	public UZ mm()
	{
		return subtract(UZ.ONE);
	}

	public UZ add(final long z)
	{
		return add(UZ.of(z));
	}

	public UZ add(final UZ uz)
	{
		final BigInteger bi = this.uz.add(uz.toBigInteger());
		return new UZ(bi);
	}

	public UZ subtract(final long z)
	{
		return subtract(UZ.of(z));
	}

	public UZ subtract(final UZ uz)
	{
		final BigInteger bi = this.uz.subtract(uz.toBigInteger());
		if(bi.signum() < 0)
			throw new ArithmeticException("Unsigned underflow");

		return new UZ(bi);
	}

	public UZ divide(final long z)
	{
		return divide(UZ.of(z));
	}

	public UZ divide(final UZ uz)
	{
		final BigInteger bi = this.uz.divide(uz.toBigInteger());
		if(bi.signum() < 0)
			throw new ArithmeticException("Unsigned underflow");

		return new UZ(bi);
	}

	public UZ multiply(final long z)
	{
		return multiply(UZ.of(z));
	}

	public UZ multiply(final UZ uz)
	{
		final BigInteger bi = this.uz.multiply(uz.toBigInteger());
		if(bi.signum() < 0)
			throw new ArithmeticException("Unsigned underflow");

		return new UZ(bi);
	}

	public UZ mod(final UZ uz)
	{
		final BigInteger bi = this.uz.mod(uz.toBigInteger());
		if(bi.signum() < 0)
			throw new ArithmeticException("Unsigned underflow");

		return new UZ(bi);
	}

	public Bytes asBytes()
	{
		return this.bytes.wrap();
	}

	public int byteLength()
	{
		return this.bytes.length();
	}

	/**
	 * Remove leading zeros of internal {@link UZ#bytes}
	 *
	 * @param length
	 * @return
	 */
	public UZ packBytes(final int length)
	{
		final byte[] bytes = ByteUtils.uzToBytes(this, length);
		return new UZ(Bytes.wrap(bytes));
	}

	public BigInteger toBigInteger()
	{
		return this.uz;
	}

	@Override
	public int compareTo(final UZ uz)
	{
		return COMPARATOR.compare(this, uz);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{value=" + FORMATTER.format(this.uz) + "}";
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(final Object o)
	{
		if(this == o)
			return true;

		if(o != null && hashCode() != o.hashCode())
			return false;

		if(o instanceof UZ)
		{
			final UZ other = (UZ) o;
			return this.uz.equals(other.uz);
		}

		return false;
	}

	public static UZ max(final UZ uz1, final UZ uz2)
	{
		if(uz1.compareTo(uz2) > 0)
			return uz1;
		return uz2;
	}

	public static UZ min(final UZ uz1, final UZ uz2)
	{
		if(uz1.compareTo(uz2) < 0)
			return uz1;
		return uz2;
	}

	private static int fillingZeros(final byte[] bytes, final int offset, final int length)
	{
		if(length <= 1)
			return 0;

		int leadingZeros = 0;
		for(int i = 0; i < length; i++)
			if(bytes[i + offset] == 0)
				leadingZeros++;
			else
				break;

		return leadingZeros;
		//return Math.max(0, leadingZeros);
	}

	/**
	 * <code>UZ.of(i)</code> is easier than <code>new UZ(i)</code> for IDE completion
	 *
	 * @param value
	 * @return
	 */
	public static UZ of(final long value)
	{
		return new UZ(value);
	}

	/**
	 * <code>UZ.of(i)</code> is easier than <code>new UZ(i)</code> for IDE completion
	 *
	 * @param value
	 * @return
	 */
	public static UZ of(final BigInteger value)
	{
		return value == null ? null : new UZ(value);
	}
}
