package io.ortis.jsak.io.bytes.array;

import java.io.IOException;

public interface ByteArray
{
	long length() throws IOException;

	long offset() throws IOException;

	int read(final byte[] buffer, final int from, int length) throws IOException;

	void seek(final long offset) throws IOException;

	default void rewind() throws IOException
	{
		seek(0);
	}

	default long skip(final long length) throws IOException
	{
		if (length < 0)
			throw new IllegalArgumentException("Skip length must be greater or equal to 0");

		if (length == 0)
			return 0;

		final long position = offset() + length;
		final long l = length();
		if (position > l)
			throw new IndexOutOfBoundsException("Invalid skip length (new position=" + position + ", length=" + l + ")");

		seek(position);
		return length;
	}
}
