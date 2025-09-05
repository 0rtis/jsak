package io.ortis.jsak.io.bytes;


import io.ortis.jsak.io.BytesProvider;
import io.ortis.jsak.io.ByteArrayChannel;
import io.ortis.jsak.util.InputValidator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;

public class ByteArray implements Bytes
{

	private final byte[] bytes;
	private transient final int hashCode;

	private ByteArray(final byte[] bytes)
	{
		this.bytes = InputValidator.checkNotNull(bytes, "bytes");
		this.hashCode = Arrays.hashCode(this.bytes);
	}

	public ByteArray concat(final ByteArray other)
	{
		return ByteArray.wrap(concat(this, other));
	}

	public byte[] head(final int length)
	{
		return Arrays.copyOfRange(this.bytes, 0, length);
	}

	public void head(final int length, final byte[] destination, final int destinationOffset)
	{
		System.arraycopy(this.bytes, 0, destination, destinationOffset, length);
	}

	public byte[] tail(final int length)
	{
		return Arrays.copyOfRange(this.bytes, this.bytes.length - length, this.bytes.length);
	}

	public void tail(final int length, final byte[] destination, final int destinationOffset)
	{
		System.arraycopy(this.bytes, this.bytes.length - length, destination, destinationOffset, length);
	}

	public byte[] read(final int offset, final int length) throws IOException
	{
		return Arrays.copyOfRange(this.bytes, offset, offset + length);
	}

	public void read(final int offset, final int length, final byte[] destination, final int destinationOffset) throws IOException
	{
		System.arraycopy(this.bytes, offset, destination, destinationOffset, length);
	}

	@Override
	public int length()
	{
		return this.bytes.length;
	}


	@Override
	public BytesProvider newBytesProvider()
	{
		return ByteArrayChannel.wrap(this.bytes);
	}

	/**
	 * Return a copy of the internal byte array
	 *
	 * @return
	 */
	@Override
	public byte[] toArray()
	{
		return toArray(0, this.bytes.length);
	}

	/**
	 * Return a copy of the internal byte array
	 *
	 * @param offset
	 * @param length
	 * @return
	 */
	@Override
	public byte[] toArray(final int offset, final int length)
	{
		final byte[] buffer = new byte[length];
		System.arraycopy(this.bytes, offset, buffer, 0, buffer.length);
		return buffer;
	}

	@Override
	public void apply(final Consumer<Byte> encoder)
	{
		for (final byte b : this.bytes)
			encoder.accept(b);
	}

	@Override
	public String toHex(final boolean withPrefix)
	{
		return withPrefix ? ByteUtil.prefixHex(ByteUtil.bytesToHex(this.bytes)) : ByteUtil.bytesToHex(this.bytes);
	}

	@Override
	public String toHex(final int offset, final int length, final boolean withPrefix)
	{
		return withPrefix ? ByteUtil.prefixHex(ByteUtil.bytesToHex(this.bytes, offset, length)) : ByteUtil.bytesToHex(this.bytes, offset, length);
	}

	@Override
	public String toString(final Charset charset)
	{
		return new String(this.bytes, charset);
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass() || hashCode() != o.hashCode())
			return false;
		final ByteArray other = (ByteArray) o;
		return Arrays.equals(this.bytes, other.bytes);
	}

	public static byte[] concat(final ByteArray head, final ByteArray tail)
	{
		return concat(head, 0, head.length(), tail, 0, tail.length());
	}

	public static byte[] concat(final ByteArray head, final int headOffset, final int headLength, final ByteArray tail, final int tailOffset,
			final int tailLength)
	{
		final byte[] concat = new byte[headLength + tailLength];
		System.arraycopy(head.bytes, headOffset, concat, 0, headLength);
		System.arraycopy(tail.bytes, tailOffset, concat, headLength, tailLength);
		return concat;
	}

	public static ByteArray wrap(final byte[] source)
	{
		return new ByteArray(source);
	}

	public static ByteArray copy(final Bytes source)
	{
		return copy(source.toArray());
	}

	public static ByteArray copy(final byte[] source)
	{
		return copy(source, 0, source.length);
	}

	public static ByteArray copy(final byte[] source, final int offset)
	{
		return copy(source, offset, source.length - offset);
	}

	public static ByteArray copy(final byte[] source, final int offset, final int length)
	{
		final byte[] buffer = new byte[length];
		System.arraycopy(source, offset, buffer, 0, length);
		return new ByteArray(buffer);
	}
}
