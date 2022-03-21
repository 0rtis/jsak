package io.ortis.jsak.io;

import java.io.IOException;
import java.io.InputStream;

public interface BytesProvider
{
	default int get(final byte[] destination) throws IOException
	{
		return get(destination, 0, destination.length);
	}

	int get(final byte[] destination, final int offset, final int length) throws IOException;

	default byte getByte() throws IOException
	{
		return (byte) get();
	}

	int get() throws IOException;

	public static BytesProvider of(final InputStream inputStream)
	{
		return new BytesProvider()
		{
			@Override
			public int get(final byte[] destination, final int offset, final int length) throws IOException
			{
				return inputStream.read(destination, offset, length);
			}

			@Override
			public int get() throws IOException
			{
				return inputStream.read();
			}
		};
	}
}
