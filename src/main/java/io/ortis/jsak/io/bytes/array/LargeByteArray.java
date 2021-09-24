package io.ortis.jsak.io.bytes.array;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RAM implementation of {@link WritableByteArray}. Max size 2^62 bytes.
 */
public class LargeByteArray implements WritableByteArray, Closeable
{
	private final int minSectorLength;
	private final List<byte[]> data;

	private long length;
	private long offset;

	private byte[] sector;
	private int sectorOffset;
	private int sectorIndex;
	private boolean closed = false;

	public LargeByteArray(final int minSectorLength)
	{
		this.minSectorLength = minSectorLength;
		if (this.minSectorLength <= 0)
			throw new IllegalArgumentException("Min sector length must be positive");

		this.data = new ArrayList<>();
		this.sector = null;
		this.sectorIndex = -1;
		this.sectorOffset = -1;
		this.length = 0;
		this.offset = 0;
	}

	private void ensureSector(final boolean create, final Integer targetLength)
	{
		if (this.sector != null && this.sectorOffset < this.sector.length)
			return;

		if (this.sector == null || this.sectorIndex >= this.data.size() - 1)
		{//create a new block
			if (create)
			{
				this.sector = new byte[Math.max(this.minSectorLength, targetLength)];
				this.data.add(this.sector);
				this.sectorIndex++;
				this.sectorOffset = 0;
			}
		} else if (this.sectorIndex < this.data.size() - 1)
		{//move to next block
			this.sector = this.data.get(++this.sectorIndex);
			this.sectorOffset = 0;
		}
	}

	@Override
	public void write(final byte b) throws IOException
	{
		if (this.closed)
			throw new IllegalStateException("Array is closed");

		ensureSector(true, 1);
		final int w = Math.min(1, this.sector.length - this.sectorOffset);
		if (w <= 0)
			throw new IOException("Failed to create new sector");

		this.sector[this.sectorOffset++] = b;

		this.offset++;
		if (this.offset > this.length)
			this.length = this.offset;
	}

	public void write(final byte[] data) throws IOException
	{
		write(data, 0, data.length);
	}

	@Override
	public void write(final byte[] data, int from, final int length) throws IOException
	{
		if (this.closed)
			throw new IllegalStateException("Array is closed");

		int remaining = length;
		while (remaining > 0)
		{
			ensureSector(true, remaining);
			final int w = Math.min(remaining, this.sector.length - this.sectorOffset);
			if (w <= 0)
				throw new IOException("Failed to create new sector");

			System.arraycopy(data, from, this.sector, this.sectorOffset, w);
			remaining -= w;
			from += w;
			this.sectorOffset += w;

			this.offset += w;
			if (this.offset > this.length)
				this.length = this.offset;
		}
	}

	public int read() throws IOException
	{
		if (this.closed)
			throw new IllegalStateException("Array is closed");

		if (this.offset >= this.length)
			return -1;

		ensureSector(false, null);

		if (this.sector.length - this.sectorOffset > 0)
		{
			this.offset++;
			return this.sector[this.sectorOffset++];
		} else
			return -1;
	}

	@Override
	public int read(final byte[] buffer, final int from, int length)
	{
		if (this.closed)
			throw new IllegalStateException("Array is closed");

		if (this.offset >= this.length)
			return -1;

		length = Math.min(length, toInt(this.length - this.offset));

		int total = 0;
		while (total < length)
		{
			ensureSector(false, null);
			final int r = Math.min(length, this.sector.length - this.sectorOffset);
			if (r <= 0)
				break;
			System.arraycopy(this.sector, this.sectorOffset, buffer, from, r);
			total += r;
			this.sectorOffset += r;

			this.offset += r;
		}

		return total;
	}

	@Override
	public void rewind() throws IOException
	{
		seek(0);
	}

	@Override
	public void seek(final long offset)
	{
		if (this.closed)
			throw new IllegalStateException("Array is closed");

		if (offset < 0 || offset > length())
			throw new IndexOutOfBoundsException("Offset out of bounds");

		long l = 0;
		for (int i = 0; i < this.data.size(); i++)
		{
			this.sector = this.data.get(i);
			this.sectorIndex = i;
			this.sectorOffset = toInt(offset - l);

			l += this.sector.length;
			if (offset < l)
				break;
		}

		this.offset = offset;
	}

	public void resize(final long length) throws IOException
	{
		if (this.closed)
			throw new IllegalStateException("Array is closed");

		final long currentLength = length();

		if (length < currentLength)
		{// trim data
			final List<byte[]> newData = new ArrayList<>();
			long l = 0;
			for (final byte[] sector : this.data)
			{
				if (l + sector.length >= length)
				{
					final byte[] newSector = new byte[toInt(length - l)];
					System.arraycopy(sector, 0, newSector, 0, newSector.length);
					newData.add(newSector);
					l += newSector.length;
					break;
				} else
				{
					newData.add(sector);
					l += sector.length;
				}
			}

			this.data.clear();
			this.data.addAll(newData);

			this.length = l;
			this.offset = this.length;

			this.sectorIndex = this.data.isEmpty() ? -1 : this.data.size() - 1;
			this.sector = this.sectorIndex < 0 ? null : this.data.get(this.sectorIndex);
			this.sectorOffset = this.sector == null ? -1 : this.sector.length;
		} else if (length > currentLength)
		{// expand data
			final byte[] buffer = new byte[Math.max(this.minSectorLength, 8192)];

			long remaining = length - this.length;
			while (remaining >= buffer.length)
			{
				this.data.add(Arrays.copyOf(buffer, buffer.length));
				remaining -= buffer.length;
			}

			this.data.add(Arrays.copyOf(buffer, toInt(Math.max(this.minSectorLength, remaining))));

			this.length = length;
		}
	}


	@Override
	public void close()
	{
		this.data.clear();
		this.offset = 0;
		this.length = 0;

		this.sector = null;
		this.sectorOffset = -1;
		this.sectorIndex = -1;

		this.closed = true;
	}

	@Override
	public long length()
	{
		return this.length;
	}

	@Override
	public long offset()
	{
		return this.offset;
	}

	public InputStream asInputStream()
	{
		return new ByteArrayInputStreamAdapter(this);
	}

	public OutputStream asOutputStream()
	{
		return new ByteArrayOutputStreamAdapter(this);
	}


	@Override
	public int hashCode()
	{
		return (int) this.length;
	}

	@Override
	public boolean equals(final Object o)
	{
		return this == o;
	}

	private static int toInt(final long value) throws ArithmeticException
	{
		if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
			throw new ArithmeticException("Value is outside int range");

		final int i = (int) value;

		if (i != value)
			throw new ArithmeticException("Value is outside int range");

		return i;
	}
}
