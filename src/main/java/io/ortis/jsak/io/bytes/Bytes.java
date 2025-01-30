package io.ortis.jsak.io.bytes;

import java.nio.charset.Charset;

public interface Bytes
{

	int length();

	byte[] toArray();

	byte[] toArray(final int offset, final int length);

	String toString(final Charset charset);

	String toString(final int offset, final int length, final Charset charset);

	default String toHex()
	{
		return ByteUtils.bytesToHexadecimal(toArray());
	}

	default String toHex(final int offset, final int length)
	{
		return ByteUtils.bytesToHexadecimal(toArray(offset, length));
	}

}

