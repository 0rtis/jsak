package io.ortis.jsak.io.bytes;


import io.ortis.jsak.util.InputValidator;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteArray implements Bytes
{

	private final byte[] bytes;
	private transient final int hashCode;

	public ByteArray(final byte[] bytes)
	{
		this.bytes = InputValidator.checkNotNull(bytes, "bytes");
		this.hashCode = Arrays.hashCode(this.bytes);
	}

	@Override
	public int length()
	{
		return this.bytes.length;
	}

	@Override
	public byte[] toArray()
	{
		return this.bytes;
	}

	@Override
	public byte[] toArray(final int offset, final int length)
	{
		final byte[] buffer = new byte[length];
		System.arraycopy(this.bytes, offset, buffer, 0, buffer.length);
		return buffer;
	}

	@Override
	public String toString(final Charset charset)
	{
		return new String(this.bytes, charset);
	}

	@Override
	public String toString(final int offset, final int length, final Charset charset)
	{
		return new String(toArray(offset, length), charset);
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
		if (!(o instanceof final ByteArray other))
			return false;

		return hashCode == other.hashCode && Arrays.equals(this.bytes, other.bytes);
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

	public static ByteArray copy(final byte[] source, final int offset, final int length)
	{
		final byte[] buffer = new byte[length];
		System.arraycopy(source, offset, buffer, 0, length);
		return new ByteArray(buffer);
	}
}
