package io.ortis.jsak.io.bytes;

import io.ortis.jsak.math.UZ;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Helper functions for {@link Byte}
 */
public abstract class ByteUtils
{
	public static final byte BITS_0 = (byte) 0;
	public static final byte BITS_1 = (byte) -1;

	static
	{
		final boolean[] bits0 = byteToBits(BITS_0);
		for (final boolean b : bits0)
			if (b)
				throw new RuntimeException("Invalid bit filler 0");

		final boolean[] bits1 = byteToBits(BITS_1);
		for (final boolean b : bits1)
			if (!b)
				throw new RuntimeException("Invalid bit filler 1");
	}

	public static byte[] copy(final byte[] value)
	{
		return copy(value, 0, value.length);
	}

	public static byte[] copy(final byte[] value, final int offset, final int length)
	{
		final byte[] copy = new byte[length];
		System.arraycopy(value, offset, copy, 0, length);
		return copy;
	}

	public static byte[] zToBytes(final long value, final int length)
	{
		return zToBytes(BigInteger.valueOf(value), length);
	}

	public static byte[] zToBytes(final BigInteger value, final int length)
	{
		final byte[] data = value.toByteArray();

		if (data.length == length)
			return data;

		return packSignedZBytes(data, length);
	}

	public static byte[] uzToBytes(final long value, final int length)
	{
		return uzToBytes(UZ.of(value), length);
	}

	public static byte[] uzToBytes(final UZ value, final int length)
	{
		final Bytes bytes = value.asBytes();
		final int diff = bytes.length() - length;
		if (diff == 0)
			return bytes.toByteArray();

		if (diff > 0)
			throw new IndexOutOfBoundsException("Cannot fit bytes in given length");

		// copy uz bytes to array pre-filled with 0
		final byte[] filled = new byte[length];
		System.arraycopy(bytes.toByteArray(), 0, filled, -diff, bytes.length());
		return filled;
		// BigInteger is signed so it will add bits(00000000) on the right side in order to insure the number is positive when deserializing
		//return zToBytes(value.toBigInteger(), length);
	}

	public static BigInteger bytesToZ(final byte[] value)
	{
		return bytesToZ(value, 0, value.length);
	}

	public static BigInteger bytesToZ(final byte[] value, final int offset, final int length)
	{
		final byte[] buffer;
		/*
		final int fillingZeros = fillingZeros(value, offset, length);

		if(fillingZeros == 0 && offset == 0 && value.length == length)
			buffer = value;
		else
		{
			buffer = new byte[length - fillingZeros];
			System.arraycopy(value, offset + fillingZeros, buffer, 0, buffer.length);
		}*/

		if (offset == 0 && value.length == length)
			buffer = value;
		else
		{
			buffer = new byte[length];
			System.arraycopy(value, offset, buffer, 0, buffer.length);
		}

		return new BigInteger(buffer);
	}

	public static UZ bytesToUZ(final byte[] value)
	{
		return bytesToUZ(value, 0, value.length);
	}

	public static UZ bytesToUZ(final byte[] value, final int offset, final int length)
	{
		return new UZ(Bytes.copy(value, offset, length));
	}

	public static BigInteger bytesToUnsignedZ(final byte[] value)
	{
		return bytesToUnsignedZ(value, 0, value.length);
	}

	public static BigInteger bytesToUnsignedZ(final byte[] value, final int offset, final int length)
	{
		if (length <= 0)
			throw new IllegalArgumentException("Invalid byte array length " + length);

		//Use hex String to read value as unsigned Z
		return new BigInteger(bytesToHexadecimal(value, offset, length), 16);
	}

	public static boolean[] bytesToBits(final byte[] bytes)
	{
		final boolean[] bits = new boolean[bytes.length * Byte.SIZE];
		for (int i = 0; i < bytes.length; i++)
		{
			//final short b = (short) (bytes[i] < 0 ? bytes[i] + 128 : bytes[i]);//Java byte are signed
			//final short b = (short) (bytes[i]+128);//Java byte are signed
			final short b = bytes[i];
			for (int j = 0; j < Byte.SIZE; j++)
				bits[i * Byte.SIZE + j] = ((b << j) & 128) == 128;
			//bits[i * Byte.SIZE + j] = ((bytes[i] >> Byte.SIZE-j-1) & 1) == 1;
		}
		return bits;
	}

	public static boolean[] byteToBits(final byte b)
	{

		final boolean[] bits = new boolean[Byte.SIZE];
		for (int j = 0; j < Byte.SIZE; j++)
			bits[j] = ((b << j) & 128) == 128;
		return bits;
	}

	public static String bytesToBitString(final byte[] bytes)
	{
		final boolean[] bits = bytesToBits(bytes);
		final StringBuilder sb = new StringBuilder();
		for (final boolean bit : bits)
			sb.append(bit ? '1' : '0');
		return sb.toString();
	}

	public static byte[] bitsToBytes(final boolean[] bits)
	{
		final byte[] bytes = new byte[bits.length / Byte.SIZE + (bits.length % Byte.SIZE == 0 ? 0 : 1)];

		for (int i = bytes.length - 1; i >= 0; i--)
		{
			//bytes[i] =(byte) (bits[i * Byte.SIZE] ? 128 : 0);
			short b = 0;
			for (int j = 0; j < Byte.SIZE; j++)
				if (bits[i * Byte.SIZE + j])
					b += 1 << (Byte.SIZE - j - 1);

			bytes[i] = (byte) b;
			//bytes[i] = (byte) (b - 128);
		}

/*
		for(int i = 0; i < bits.length; i++)
			bytes[i / Byte.SIZE] +=
		//	if(bits[i])
		//		bytes[i / Byte.SIZE] = (byte) (bytes[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
*/
		return bytes;
	}


/*
	public static BigInteger bytesToUnsignedZ(final byte[] value)
	{
		return bytesToUnsignedZ(value, 0, value.length);
	}

	public static BigInteger bytesToUnsignedZ(final byte[] value, final int offset, final int length)
	{
		final BigInteger twoCompl = BigInteger.ONE.shiftLeft(length << 3);

		BigInteger bi = bytesToZ(value, offset, length);
		if(bi.compareTo(BigInteger.ZERO) < 0)
		{
			bi = bi.add(twoCompl);
			if(bi.compareTo(twoCompl) >= 0 || bi.compareTo(BigInteger.ZERO) < 0)
				throw new ArithmeticException("Out of range");
		}
		return bi;
	}*/

	/**
	 * Convert signed bytes (Java style) to unsigned bytes (C style)
	 *
	 * @param data
	 * @return
	 */
	public static short[] bytesToUnsignedShortArray(final byte[] data)
	{
		return bytesToUnsignedShortArray(data, new short[data.length]);
	}

	/**
	 * Convert signed bytes (Java style) to unsigned bytes (C style)
	 *
	 * @param data
	 * @param destination
	 * @return
	 */
	public static short[] bytesToUnsignedShortArray(final byte[] data, final short[] destination)
	{
		if (data.length != destination.length)
			throw new IllegalArgumentException("Size mismatch");

		for (int i = 0; i < data.length; i++)
			destination[i] = byteToUnsignedShort(data[i]);

		return destination;
	}

	/**
	 * Convert signed byte (Java style) to unsigned byte (C style)
	 *
	 * @param b
	 * @return
	 */
	public static short byteToUnsignedShort(final byte b)
	{
		return (short) (b & 0xff);
	}

		/*
	public static int fillingZeros(final byte[] bytes)
	{
		return fillingZeros(bytes, 0, bytes.length);
	}


	public static int fillingZeros(final byte[] bytes, final int offset, final int length)
	{
		int fillingZeros = -1;
		for(int i = 0; i < length; i++)
			if(bytes[i + offset] == 0)
				fillingZeros++;
			else
				break;

		return Math.max(fillingZeros, 0);
	}*/


	/*public static byte[] pack(final byte[] bytes)
	{
		final int fillingZeros = fillingZeros(bytes);

		if(fillingZeros == 0)
			return Arrays.copyOf(bytes, bytes.length);

		return pack(bytes, bytes.length - fillingZeros);
	}


	public static byte[] pack(final byte[] zBytes, final int packLength)
	{
		final int diff = zBytes.length - packLength;
		final byte[] pack = new byte[packLength];

		if(diff > 0)
		{// bytes > pack

			final byte[] trim = new byte[diff];
			System.arraycopy(zBytes, 0, trim, 0, diff);

			final BigInteger bi = new BigInteger(trim);
			if(bi.compareTo(BigInteger.ZERO) != 0)
				throw new IndexOutOfBoundsException("Cannot fit bytes in given length");

			System.arraycopy(zBytes, diff, pack, 0, packLength);
		}
		else
			System.arraycopy(zBytes, 0, pack, -diff, zBytes.length); // bytes < pack (leading space is filled with zeros)

		return pack;
	}
*/


	/**
	 * Store byte array of signed Z into a specific length byte array.
	 * The method preserve the sign of Z.
	 *
	 * @param bytes
	 * @param packLength
	 * @return
	 */
	public static byte[] packSignedZBytes(final byte[] bytes, final int packLength)
	{
		final int diff = bytes.length - packLength;
		final byte[] pack = new byte[packLength];

		if (diff > 0)
		{// bytes > pack

			final byte[] trim = new byte[diff];
			System.arraycopy(bytes, 0, trim, 0, diff);
			System.arraycopy(bytes, diff, pack, 0, packLength);
			final boolean[] packMSB = byteToBits(pack[0]);

			Byte trimByte = null;
			for (final byte b : trim)
				if (trimByte == null)
					trimByte = b;
				else if (b != trimByte)
					throw new IndexOutOfBoundsException("Cannot fit bytes in given length (different byte value in trim section)");

			if (trimByte == null)
				throw new RuntimeException("trimByte is null");

			if (trimByte != BITS_0 && trimByte != BITS_1)
				throw new IndexOutOfBoundsException(
						"Cannot fit bytes in given length (byte value in trim is " + trimByte + ", expected " + BITS_0 + " or " + BITS_1 + ")");

			final boolean[] trimBit = byteToBits(trimByte);

			if (trimBit[0] != packMSB[0])
				throw new IndexOutOfBoundsException("Cannot fit bytes in given length (LSB trim and MSB pack do not match)");
		} else
		{
			final boolean[] MSB = byteToBits(bytes[0]);
			final byte fill = MSB[0] ? BITS_1 : BITS_0;
			Arrays.fill(pack, fill);
			System.arraycopy(bytes, 0, pack, -diff, bytes.length); // bytes < pack
		}

		return pack;
	}

	public static String bytesToHexadecimal(final byte[] bytes)
	{
		return bytesToHexadecimal(bytes, 0, bytes.length);
	}

	public static String bytesToHexadecimal(final byte[] bytes, final int offset, final int length)
	{
		if (length <= 0)
			throw new IllegalArgumentException("Length must be greater than 0");

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++)
			sb.append(String.format("%02X", bytes[offset + i]));

		return sb.toString();
	}

	public static byte[] hexadecimalToBytes(final String hex)
	{
		if (hex.length() % 2 != 0)
			throw new IllegalArgumentException("Hexadecimal string length must be even");
		final int byteLength = hex.length() >> 1;
		final byte[] bytes = new byte[byteLength];
		final byte[] bigIntBytes = new BigInteger(hex, 16).toByteArray();
		int leadingZeros = 0;
		if (bigIntBytes.length > bytes.length)
			for (; leadingZeros < bigIntBytes.length; leadingZeros++)
				if (bigIntBytes[leadingZeros] != 0)
					break;
		System.arraycopy(bigIntBytes, leadingZeros, bytes, bytes.length - (bigIntBytes.length - leadingZeros), bigIntBytes.length - leadingZeros);
		return bytes;
	}
}