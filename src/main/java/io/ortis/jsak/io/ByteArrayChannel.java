package io.ortis.jsak.io;

import io.ortis.jsak.io.bytes.ByteUtil;

import java.io.IOException;

public class ByteArrayChannel implements BytesProvider, BytesConsumer
{
	private final byte[] bytes;

	private int readIndex = 0;
	private int writeIndex = 0;


	private ByteArrayChannel(final byte[] bytes)
	{
		this.bytes = bytes;
	}

	public int length()
	{
		return this.bytes.length;
	}

	public int readPosition()
	{
		return this.readIndex;
	}

	public void seekRead(final int position)
	{
		if (position < 0 || position >= this.bytes.length)
			throw new IllegalArgumentException("Invalid position");
		this.readIndex = position;
	}

	public void resetRead()
	{
		seekRead(0);
	}


	public int writePosition()
	{
		return this.writeIndex;
	}

	public void seekWrite(final int position)
	{
		if (position < 0 || position >= this.bytes.length)
			throw new IllegalArgumentException("Invalid position");
		this.writeIndex = position;
	}

	public void resetWrite()
	{
		seekWrite(0);
	}

	@Override
	public int get(final byte[] destination, final int offset, final int length) throws IOException
	{
		if (this.readIndex >= this.bytes.length)
			return -1;

		final int len = Math.min(length, this.bytes.length - this.readIndex);
		System.arraycopy(this.bytes, this.readIndex, destination, offset, len);
		this.readIndex += len;
		return len;
	}

	@Override
	public int get() throws IOException
	{
		if (this.readIndex >= this.bytes.length)
			return -1;

		return this.bytes[this.readIndex++];
	}

	@Override
	public void accept(final byte[] data, final int offset, final int length) throws IOException
	{
		final int len = Math.min(length, this.bytes.length - this.writeIndex);
		if (this.writeIndex + len > this.bytes.length)
			throw new IOException("End of channel");
		System.arraycopy(data, offset, this.bytes, this.writeIndex, len);
		this.writeIndex += len;
	}

	@Override
	public void accept(final int data) throws IOException
	{
		if (this.writeIndex >= this.bytes.length)
			throw new IOException("End of channel");

		this.bytes[this.writeIndex++] = (byte) data;
	}

	public static ByteArrayChannel wrap(final byte[] bytes)
	{
		return new ByteArrayChannel(bytes);
	}

	public static ByteArrayChannel copy(final byte[] bytes)
	{
		return new ByteArrayChannel(ByteUtil.copy(bytes, 0, bytes.length));
	}
}
