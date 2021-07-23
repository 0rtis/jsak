package jsak.io.bytes;

import java.io.IOException;

public interface ByteArray
{
	long length() throws  IOException;

	long offset() throws IOException;

	int read(final byte[] buffer, final int from, int length) throws IOException;

	void rewind() throws IOException;

	void seek(final long offset) throws IOException;
}
