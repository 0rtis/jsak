package io.ortis.jsak.io.bytes;


import io.ortis.jsak.util.InputValidator;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Helper functions for {@link Byte}
 */
public abstract class ByteUtil
{
	private static final char[] HEX_CHARACTERS = "0123456789abcdef".toCharArray();

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

	public static byte[] copy(final byte[] value, final int offset)
	{
		return copy(value, offset, value.length - offset);
	}

	public static byte[] copy(final byte[] value, final int offset, final int length)
	{
		final byte[] copy = new byte[length];
		System.arraycopy(value, offset, copy, 0, length);
		return copy;
	}


	public static byte[] concat(final byte[] head, final byte[] tail)
	{
		return concat(head, 0, head.length, tail, 0, tail.length);
	}

	public static byte[] concat(final byte[] head, final int headOffset, final int headLength, final byte[] tail, final int tailOffset,
			final int tailLength)
	{
		final byte[] concat = new byte[headLength + tailLength];
		System.arraycopy(head, headOffset, concat, 0, headLength);
		System.arraycopy(tail, tailOffset, concat, headLength, tailLength);
		return concat;
	}

	public static byte[] unsignedZToBytes(final long value)
	{
		return unsignedZToBytes(BigInteger.valueOf(value));
	}

	public static byte[] unsignedZToBytes(final long value, final int length)
	{
		return unsignedZToBytes(BigInteger.valueOf(value), length, new byte[length], 0);
	}

	/**
	 * Convert a positive BigInteger to a byte array without guaranteeing the length
	 *
	 * @param value
	 * @return
	 */
	public static byte[] unsignedZToBytes(final BigInteger value)
	{
		final byte[] data = value.toByteArray();
		int off = 0;
		if (data.length > 1)
			for (int i = 0; i < data.length; i++, off++)
				if (data[i] != 0)
					break;
		if (off == 0)
			return data;
		final byte[] buffer = new byte[data.length - off];
		System.arraycopy(data, off, buffer, 0, buffer.length);
		return buffer;
	}


	public static byte[] unsignedZToBytes(final BigInteger value, final int length)
	{
		return unsignedZToBytes(value, length, new byte[length], 0);
	}


	public static byte[] unsignedZToBytes(final BigInteger value, final int length, final byte[] destination, final int offset)
	{
		final byte[] data = value.toByteArray();

		if (data.length == length)
			System.arraycopy(data, 0, destination, offset, length);
		else
		{
			final byte[] buffer = new byte[length];

			if (length > data.length)
			{
				System.arraycopy(data, 0, buffer, buffer.length - data.length, data.length);
				System.arraycopy(buffer, 0, destination, offset, length);
			} else
			{
				int off = 0;
				if (data.length > 1)
					for (int i = 0; i < data.length; i++, off++)
						if (data[i] != 0)
							break;

				if (length >= data.length - off)
					System.arraycopy(data, off, destination, offset, data.length - off);
				else
					throw new ArrayIndexOutOfBoundsException("Cannot fit value in given length");
			}
		}


		return destination;
	}

	public static byte[] signedZToBytes(final long value)
	{
		return signedZToBytes(BigInteger.valueOf(value));
	}

	public static byte[] signedZToBytes(final long value, final int length)
	{
		return signedZToBytes(BigInteger.valueOf(value), length, new byte[length], 0);
	}

	/**
	 * Convert a BigInteger to a byte array without guaranteeing the length
	 *
	 * @param value
	 * @return
	 */
	public static byte[] signedZToBytes(final BigInteger value)
	{
		return value.toByteArray();
	}

	public static byte[] signedZToBytes(final BigInteger value, final int length)
	{
		return signedZToBytes(value, length, new byte[length], 0);
	}

	public static byte[] signedZToBytes(final BigInteger value, final int length, final byte[] destination, final int offset)
	{
		final byte[] data = value.toByteArray();
		if (data.length == length)
			System.arraycopy(data, 0, destination, offset, length);
		else
			packSignedZBytes(data, length, destination, offset);

		return destination;
	}


	public static BigInteger bytesToSignedZ(final byte[] value)
	{
		return bytesToSignedZ(value, 0, value.length);
	}

	public static BigInteger bytesToSignedZ(final byte[] value, final int offset, final int length)
	{
		if (length <= 0)
			throw new IllegalArgumentException("Invalid byte array length " + length);

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


	public static BigInteger bytesToUnsignedZ(final byte[] value)
	{
		return bytesToUnsignedZ(value, 0, value.length);
	}

	public static BigInteger bytesToUnsignedZ(final byte[] value, final int offset, final int length)
	{
		if (length <= 0)
			throw new IllegalArgumentException("Invalid byte array length " + length);

		//Use hex String to read value as unsigned Z
		return new BigInteger(bytesToHex(value, offset, length), 16);
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


	public static byte[] packSignedZBytes(final byte[] bytes, final int packLength)
	{
		final byte[] pack = new byte[packLength];
		packSignedZBytes(bytes, packLength, pack, 0);
		return pack;
	}

	/**
	 * Store byte array of signed Z into a specific length byte array.
	 * The method preserve the sign of Z.
	 *
	 * @param bytes
	 * @param packLength
	 * @param destination
	 * @param offset
	 */
	public static void packSignedZBytes(final byte[] bytes, final int packLength, final byte[] destination, final int offset)
	{
		final int diff = bytes.length - packLength;


		if (diff > 0)
		{// bytes > pack

			final byte[] trim = new byte[diff];
			System.arraycopy(bytes, 0, trim, 0, diff);
			System.arraycopy(bytes, diff, destination, offset, packLength);
			final boolean[] packMSB = byteToBits(destination[offset]);

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
			Arrays.fill(destination, offset, offset + packLength, fill);
			System.arraycopy(bytes, 0, destination, offset - diff, bytes.length); // bytes < pack
		}

	}


	public static String unsignedZToHex(final long value, final int byteLength)
	{
		return unsignedZToHex(BigInteger.valueOf(value), byteLength);
	}


	public static String unsignedZToHex(final BigInteger value, final int byteLength)
	{
		return bytesToHex(unsignedZToBytes(value, byteLength));
		/*
		if (byteLength != -1)
			InputValidator.checkStrictlyPositive(byteLength, "byteLength");

		final StringBuilder base16 = new StringBuilder();
		final byte[] bytes = value.toByteArray();
		int offset = 0;
		if (bytes.length > 1)
			for (int i = 0; i < bytes.length; i++, offset++)
				if (bytes[i] != 0)
					break;

		if (offset > 0)
			offset--;// keep the sign bit

		final int adjByteLength = bytes.length - offset;
		if (byteLength != -1)
		{
			if (adjByteLength < byteLength)
				base16.append("00".repeat(byteLength - adjByteLength));
			else if (adjByteLength > byteLength)
				throw new IllegalArgumentException("Overflow: bytes length exceed %d".formatted(byteLength));
		}

		for (int i = offset; i < bytes.length; i++)
			base16.append(String.format("%02x", bytes[i]));
		return base16.toString();*/
	}

	public static String signedZToHex(final long value, final int byteLength)
	{
		return signedZToHex(BigInteger.valueOf(value), byteLength);
	}

	public static String signedZToHex(final BigInteger value, final int byteLength)
	{
		return bytesToHex(signedZToBytes(value, byteLength));
	}


	public static String bytesToHex(final byte[] bytes)
	{
		return bytesToHex(bytes, 0, bytes.length);
	}

	public static String bytesToHex(final byte[] bytes, final int offset, final int length)
	{
		/*if (length <= 0)
			throw new IllegalArgumentException("Length must be greater than 0");

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++)
			sb.append(String.format("%02x", bytes[offset + i]));

		return sb.toString();*/

		final char[] buffer = new char[length * 2];

		for (int i = 0; i < length; i++)
		{
			int v = bytes[offset + i] & 0xFF;
			buffer[i * 2] = HEX_CHARACTERS[v >>> 4];
			buffer[i * 2 + 1] = HEX_CHARACTERS[v & 0x0F];
		}
		return new String(buffer);
	}
/*
	public static byte[] hexToBytes(final String hex)
	{
		InputValidator.check(hex.length() % 2 == 0, "Hexadecimal string must be of even length");
		final byte[] bytes = new byte[hex.length() >> 1];
		for (int i = 0; i < hex.length(); i += 2)
			bytes[i >> 1] = (byte) ((hexCharTo4BitWord(hex.charAt(i)) << 4) | hexCharTo4BitWord(hex.charAt(i + 1)));

		return bytes;
	}*/

	public static byte[] hexToBytes(final String hex)
	{
		final int len = hex.length();
		final byte[] bytes = new byte[(len + 1) >> 1];

		final int offset = len % 2;
		if (offset == 1)
			bytes[0] = (byte) hexCharTo4BitWord(hex.charAt(0));

		for (int i = offset; i < len; i += 2)
			bytes[(i + 1) >> 1] = (byte) ((hexCharTo4BitWord(hex.charAt(i)) << 4) | hexCharTo4BitWord(hex.charAt(i + 1)));

		return bytes;
	}
/*
	public static int hexToBytes(final String hex, final byte[] destination, final int offset)
	{
		InputValidator.check(hex.length() % 2 == 0, "Hexadecimal string must be of even length");
		for (int i = 0; i < hex.length(); i += 2)
			destination[offset + (i >> 1)] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		return hex.length() >> 1;
	}
*/

	public static int hexToBytes(final String hex, final byte[] destination, final int offset)
	{
		final int len = hex.length();

		final int off = len % 2;
		if (off == 1)
			destination[offset] = (byte) hexCharTo4BitWord(hex.charAt(0));

		for (int i = off; i < len; i += 2)
			destination[offset + ((i + 1) >> 1)] = (byte) ((hexCharTo4BitWord(hex.charAt(i)) << 4) | hexCharTo4BitWord(hex.charAt(i + 1)));

		return (len + 1) >> 1;

	}

	private static int hexCharTo4BitWord(final char c)
	{
		if (c >= '0' && c <= '9')
			return c - '0';
		if (c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		if (c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		throw new IllegalArgumentException("Invalid hex character: %s".formatted(c));
	}

	public static BigInteger hexToUnsignedZ(final String hex)
	{
		//InputValidator.check(hex.length() % 2 == 0, "Hexadecimal string must be of even length");
		return new BigInteger(hex, 16);

	}

	public static BigInteger hexToSignedZ(final String hex)
	{
		InputValidator.check(hex.length() % 2 == 0, "Signed z hexadecimal string must be of even length");
		final int inputBits = (hex.length()) << 2;
		/*if (bits > inputBits)
			bits = inputBits;*/
		BigInteger bigInteger = new BigInteger(hex, 16);
		if (bigInteger.signum() != 0 && bigInteger.and(BigInteger.ONE.shiftLeft(inputBits - 1)).signum() != 0)
			bigInteger = bigInteger.subtract(BigInteger.ONE.shiftLeft(inputBits));

		return bigInteger;
	}

	/**
	 * Return a prefixed hex string
	 *
	 * @param hex
	 * @return
	 */
	public static String ensurePrefixHex(final String hex)
	{
		if (hex.startsWith("0x"))
			return hex;
		return prefixHex(hex);
	}

	/**
	 * Add a prefix to a hex string
	 *
	 * @param unprefixedHex
	 * @return
	 */
	public static String prefixHex(final String unprefixedHex)
	{
		return "0x%s".formatted(unprefixedHex);
	}

	/**
	 * Return an unprefixed hex string
	 *
	 * @param hex
	 * @return
	 */
	public static String ensureUnprefixHex(final String hex)
	{
		if (hex.startsWith("0x"))
			return unprefixHex(hex);
		return hex;
	}

	/**
	 * Remove the prefix from a hex string
	 *
	 * @param prefixedHex
	 * @return
	 */
	public static String unprefixHex(final String prefixedHex)
	{
		return prefixedHex.substring(2);
	}

}