package io.ortis.jsak.io.bytes.array;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class ByteArrayInputStreamAdapter extends InputStream
{
	private final LargeByteArray largeByteArray;

	public ByteArrayInputStreamAdapter(final LargeByteArray largeByteArray)
	{
		this.largeByteArray = largeByteArray;
	}

	@Override
	public int read() throws IOException
	{
		return this.largeByteArray.read();
	}

	@Override
	public int read(final byte[] b) throws IOException
	{
		return this.largeByteArray.read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException
	{
		return this.largeByteArray.read(b, off, len);
	}

	@Override
	public int available()
	{
		return BigInteger.valueOf(this.largeByteArray.length() - this.largeByteArray.offset()).intValueExact();
	}

	@Override
	public void close()
	{
		this.largeByteArray.close();
	}
}
