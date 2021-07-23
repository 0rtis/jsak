package jsak.io.bytes;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * RAM implementation of {@link WritableByteArray}. Not limited in size.
 */
public class RAMByteArray implements WritableByteArray
{
	private final int minSectorLength;
	private final List<byte[]> data;

	private long usedLength;
	private long length;
	private long offset;

	private byte[] sector;
	private int sectorOffset;
	private int sectorIndex;

	public RAMByteArray(final int minSectorLength)
	{
		this.minSectorLength = minSectorLength;
		if(this.minSectorLength <= 0)
			throw new IllegalArgumentException("Min sector length must be positive");

		this.data = new ArrayList<>();
		this.sector = null;
		this.sectorIndex = -1;
		this.sectorOffset = -1;
		this.usedLength = 0;
		this.length = 0;
		this.offset = 0;
	}

	private void ensureSector(final boolean create, final Integer targetLength)
	{
		if(this.sector != null && this.sectorOffset < this.sector.length)
			return;

		if(this.sector == null || this.sectorIndex >= this.data.size() - 1)
		{//create a new block
			if(create)
			{
				this.sector = new byte[Math.max(this.minSectorLength, targetLength)];
				this.usedLength += this.sector.length;
				this.data.add(this.sector);
				this.sectorIndex++;
				this.sectorOffset = 0;
			}
		}
		else if(this.sectorIndex < this.data.size() - 1)
		{//move to next block
			this.sector = this.data.get(++this.sectorIndex);
			this.sectorOffset = 0;
		}
	}

	@Override
	public void write(final byte[] buffer, final int from, final int length) throws IOException
	{
		if(this.offset + length > length())
			throw new IOException("Buffer overflow");

		int remaining = length;
		while(remaining > 0)
		{
			ensureSector(true, remaining);
			final int w = Math.min(length, this.sector.length - this.sectorOffset);
			if(w <= 0)
				throw new IOException("Failed to create new sector");

			System.arraycopy(buffer, from, this.sector, this.sectorOffset, w);
			remaining -= w;
			this.sectorOffset += w;

			this.offset += w;
			if(this.offset > this.length)
				this.length = this.offset;
		}
	}

	@Override
	public int read(final byte[] buffer, final int from, int length)
	{
		if(this.offset >= this.length)
			return -1;

		int total = 0;
		while(total < length)
		{
			ensureSector(false, null);
			final int r = Math.min(length, this.sector.length - this.sectorOffset);
			if(r <= 0)
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
	public void seek(final long offset) throws IOException
	{
		if(offset < 0 || offset >= length())
			throw new IOException("Offset out of bounds");

		long l = 0;
		for(int i = 0; /*no upper bound - we want the exception if the code is wrong*/ ; i++)
		{
			final byte[] sector = this.data.get(i);
			l += sector.length;

			if(offset < l)
			{
				this.sector = sector;
				this.sectorIndex = i;
				this.sectorOffset = toInt(offset - (l - sector.length));
				break;
			}
		}

		this.offset = offset;
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
	{//TODO: test speed of both
		if(value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
			throw new ArithmeticException("Value is outside int range");

		final int i = (int) value;

		if(i != value)
			throw new ArithmeticException("Value is outside int range");

		return BigInteger.valueOf(value).intValueExact();
	}
}
