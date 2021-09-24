package io.ortis.jsak.io.bytes.array;

import java.io.IOException;

public interface WritableByteArray extends ByteArray
{
	void write(final byte b) throws IOException;

	void write(final byte[] buffer, final int from, final int length) throws IOException;
}
