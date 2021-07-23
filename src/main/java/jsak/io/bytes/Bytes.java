package jsak.io.bytes;


import java.io.IOException;
import java.util.Arrays;

/**
 * An immutable wrapper of <code>byte []</code>
 */
public class Bytes
{
	private final byte[] data;
	private final int dataOffset;
	private final int dataLength;

	private transient final int hashCode;

	private transient int position;

	private Bytes(final byte[] data, final int dataOffset, final int dataLength)
	{
		if(data == null)
			throw new IllegalArgumentException("data is null");

		this.data = data;
		this.dataOffset = dataOffset;
		this.dataLength = dataLength;

		//fast hash code for byte array
		final int m = 31;
		int hashCodeBuffer = m * this.dataLength;
		for(int i = 0; i < this.dataLength; i++)
		{
			if(i <= 3 || i == 5 || i == 8 || i == 13 || i == 21)
				hashCodeBuffer += m * hashCodeBuffer + this.data[this.dataOffset + i];
			if(i >= 21)
				break;
		}

		this.hashCode = hashCodeBuffer;
		this.position = 0;
	}

	public byte read() throws IOException
	{
		final int remaining = this.dataLength - this.position;
		if(remaining <= 0)
			throw new IOException("End of bytes");

		return this.data[this.position++];
	}

	public int read(final byte[] buffer, final int offset, int length)
	{
		final int remaining = this.dataLength - this.position;
		if(remaining <= 0)
			return -1;

		length = Math.min(remaining, length);

		System.arraycopy(this.data, this.dataOffset + this.position, buffer, offset, length);
		this.position += length;

		return length;
	}

	public Bytes rewind()
	{
		this.position = 0;
		return this;
	}

	public Bytes seek(final int position)
	{
		if(position < 0 || position >= length())
			throw new IllegalArgumentException("Position out of bounds");

		this.position = position;
		return this;
	}

	public int length()
	{
		return this.dataLength;
	}

	public int getPosition()
	{
		return this.position;
	}

	public byte[] toByteArray()
	{
		return ByteUtils.copy(this.data, this.dataOffset, this.dataLength);
	}

	/**
	 * Self wrap. Create a new {@link Bytes} using the same underlying byte []
	 *
	 * @return
	 */
	public Bytes wrap()
	{
		return wrap(this);
	}

	public Bytes copy()
	{
		return Bytes.wrap(toByteArray());
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(final Object o)
	{
		if(this == o)
			return true;

		if(o != null && hashCode() != o.hashCode())
			return false;

		if(o instanceof Bytes)
		{
			final Bytes other = (Bytes) o;

			if(this.dataLength != other.dataLength)
				return false;

			for(int i = 0; i < this.dataLength; i++)
				if(this.data[this.dataOffset + i] != other.data[other.dataOffset + i])
					return false;

			return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" +
				"base16=" + (this.dataLength <= 0 ? null : ByteUtils.bytesToHexadecimal(this.data, this.dataOffset, this.dataLength)) +
				", dataOffset=" + this.dataOffset +
				", dataLength=" + this.dataLength +
				"}";
	}

	public static Bytes wrap(final byte b)
	{
		return wrap(new byte[]{b});
	}

	/**
	 * WARNING: do not change the input <code>byte []</code> afterwards or it will invalid <code>hash() & equals()</code>
	 *
	 * @param data
	 * @return
	 */
	public static Bytes wrap(final byte[] data)
	{
		return wrap(data, 0, data.length);
	}

	/**
	 * WARNING: do not change the input <code>byte []</code> afterwards or it will invalid <code>hash() & equals()</code>
	 *
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */
	public static Bytes wrap(final byte[] buffer, final int offset, final int length)
	{
		return new Bytes(buffer, offset, length);
	}

	public static Bytes wrap(final Bytes bytes)
	{
		return wrap(bytes.data, bytes.dataOffset, bytes.data.length);
	}

	public static Bytes copy(final byte[] data)
	{
		return copy(data, 0, data.length);
	}

	public static Bytes copy(final byte[] buffer, final int offset, final int length)
	{
		final byte[] copy = ByteUtils.copy(buffer, offset, length);
		return new Bytes(copy, 0, copy.length);
	}

	public static void destroy(final Bytes bytes)
	{
		Arrays.fill(bytes.data, bytes.dataOffset, bytes.dataOffset + bytes.dataLength, (byte) 0);
	}
}
