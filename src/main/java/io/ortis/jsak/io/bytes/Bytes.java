package io.ortis.jsak.io.bytes;

import io.ortis.jsak.io.BytesConsumer;
import io.ortis.jsak.io.BytesProvider;

import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Bytes
{

	int length();

	BytesProvider newBytesProvider();

	byte[] toArray();

	byte[] toArray(final int offset, final int length);

	void apply(final Consumer<Byte> encoder);

	default String toHex(final boolean withPrefix)
	{
		return withPrefix ? ByteUtil.prefixHex(ByteUtil.bytesToHex(toArray())) : ByteUtil.bytesToHex(toArray());
	}

	default String toHex(final int offset, final int length, final boolean withPrefix)
	{
		return withPrefix ? ByteUtil.prefixHex(ByteUtil.bytesToHex(toArray(offset, length))) : ByteUtil.bytesToHex(toArray(offset, length));
	}


	default String toString(final Charset charset)
	{
		return new String(toArray(), charset);
	}

	default String toString(final int offset, final int length, final Charset charset)
	{
		return new String(toArray(offset, length), charset);
	}

}

