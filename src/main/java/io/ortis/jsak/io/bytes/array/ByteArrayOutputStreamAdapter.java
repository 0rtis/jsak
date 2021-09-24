package io.ortis.jsak.io.bytes.array;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayOutputStreamAdapter extends OutputStream
{
	private final LargeByteArray largeByteArray;

	public ByteArrayOutputStreamAdapter(final LargeByteArray largeByteArray)
	{
		this.largeByteArray = largeByteArray;
	}

	@Override
	public void write(final int b) throws IOException
	{
		this.largeByteArray.write((byte)b);
	}

	@Override
	public void write(final byte[] b) throws IOException
	{
		this.largeByteArray.write(b, 0, b.length);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException
	{
		this.largeByteArray.write(b, off, len);
	}

	@Override
	public void close() throws IOException
	{
		this.largeByteArray.close();
	}
}
