package io.ortis.jsak.io;

import java.io.IOException;
import java.io.OutputStream;

public interface BytesConsumer
{
	default void accept(final byte[] data) throws IOException
	{
		accept(data, 0, data.length);
	}

	void accept(final byte[] data, final int offset, final int length) throws IOException;

	default void accept(final byte data) throws IOException
	{
		accept((int) data);
	}

	void accept(final int data) throws IOException;

	public static BytesConsumer of(final OutputStream outputStream)
	{
		return new BytesConsumer()
		{
			@Override
			public void accept(final byte[] data, final int offset, final int length) throws IOException
			{
				outputStream.write(data, offset, length);
			}

			@Override
			public void accept(final int data) throws IOException
			{
				outputStream.write(data);
			}
		};
	}
}
