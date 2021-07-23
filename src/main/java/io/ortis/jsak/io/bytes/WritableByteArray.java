package io.ortis.jsak.io.bytes;

import java.io.IOException;

public interface WritableByteArray extends ByteArray
{
	void write(final byte[] buffer, final int from, final int length) throws IOException;
}
