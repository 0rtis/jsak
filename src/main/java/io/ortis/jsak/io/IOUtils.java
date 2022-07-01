package io.ortis.jsak.io;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

/**
 * Helper functions for I/O operation
 */
public abstract class IOUtils
{
	private static final int MAX_INT = Integer.MAX_VALUE - 1;

	public static byte[] readResource(final String path, final byte[] buffer, final int offset, final int length) throws IOException
	{
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			readResource(path, baos, buffer, offset, length);
			return baos.toByteArray();
		}
	}

	public static void readResource(final String path, final OutputStream destination, final byte[] buffer, final int offset, final int length)
			throws IOException
	{
		try (final InputStream is = IOUtils.class.getResourceAsStream(path))
		{
			if (is == null)
				throw new IOException("Resource not found");

			stream(is, destination, buffer, offset, length);
		}
	}


	public static File[] listResourceFolder(String folderPath)
	{
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		final URL url = loader.getResource(folderPath);
		return new File(url.getPath()).listFiles();
	}


	public static void readExact(final int length, final Path path, final byte[] destination, final int destinationOffset) throws IOException
	{
		try (final RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r"))
		{
			readExact(length, raf, destination, destinationOffset);
		}
	}

	public static void readExact(final int length, final RandomAccessFile randomAccessFile, final byte[] destination, final int destinationOffset)
			throws IOException
	{
		int total = 0;
		while (total < length)
		{
			final int read = randomAccessFile.read(destination, destinationOffset + total, length - total);
			if (read <= 0)
				break;

			total += read;
		}

		if (total != length)
			throw new IOException("Total read mismatch (expected " + length + " but read " + total + ")");
	}

	public static void readExact(final int length, final RandomAccessFile randomAccessFile, final OutputStream destination, final byte[] buffer)
			throws IOException
	{
		readExact(length, randomAccessFile, BytesConsumer.of(destination), buffer, 0, buffer.length);
	}

	public static void readExact(final int length, final RandomAccessFile randomAccessFile, final BytesConsumer destination, final byte[] buffer,
			final int bufferOffset, final int bufferLength) throws IOException
	{
		if (bufferLength == 0)
			throw new IllegalArgumentException("Buffer length must be greater than 0");

		int read;
		int total = 0;
		while (total < length)
		{
			read = randomAccessFile.read(buffer, bufferOffset, Math.min(length - total, bufferLength));

			if (read < 0 || (read == 0 && randomAccessFile.getFilePointer() >= randomAccessFile.length()))
				throw new IOException("End of file before target length was reached");

			destination.accept(buffer, bufferOffset, read);
			total += read;
		}

		if (total != length)
			throw new IOException("Length mismatch (expected " + length + " but read " + total + ")");
	}

	public static int readAtMost(final int length, final RandomAccessFile randomAccessFile, final byte[] destination, final int destinationOffset)
			throws IOException
	{
		int total = 0;
		while (total < length)
		{
			final int read = randomAccessFile.read(destination, destinationOffset + total, length - total);
			if (read <= 0)
				break;

			total += read;
		}

		return total;
	}

	public static int stream(final InputStream source, final byte[] destination) throws IOException
	{
		return stream(BytesProvider.of(source), destination, 0, destination.length);
	}

	public static int stream(final InputStream source, final byte[] destination, final int destinationOffset, int destinationLength)
			throws IOException
	{
		return stream(BytesProvider.of(source), destination, destinationOffset, destinationLength);
	}

	public static int stream(final BytesProvider source, final byte[] destination, final int destinationOffset, int destinationLength)
			throws IOException
	{
		int read = 0;
		int total = 0;
		while (total < destinationLength && (read = source.get(destination, destinationOffset + total, destinationLength - total)) > -1)
			total += read;

		if (read < 0 && total == 0)
			return read;

		return total;
	}


	public static int stream(final InputStream source, final OutputStream destination, final byte[] buffer) throws IOException
	{
		return stream(source, destination, buffer, 0, buffer.length);
	}

	public static int stream(final InputStream source, final OutputStream destination, final byte[] buffer, final int bufferLength) throws IOException
	{
		return stream(source, destination, buffer, 0, bufferLength);
	}

	public static int stream(final InputStream source, final OutputStream destination, final byte[] buffer, final int bufferOffset, int bufferLength)
			throws IOException
	{
		return stream(BytesProvider.of(source), BytesConsumer.of(destination), buffer, bufferOffset, bufferLength);
	}

	public static int stream(final BytesProvider source, final BytesConsumer destination, final byte[] buffer) throws IOException
	{
		return stream(source, destination, buffer, 0, buffer.length);
	}

	public static int stream(final BytesProvider source, final BytesConsumer destination, final byte[] buffer, final int bufferOffset,
			int bufferLength) throws IOException
	{
		int read = 0;
		int total = 0;
		while (bufferLength > 0 && (read = source.get(buffer, bufferOffset, bufferLength)) > -1)
		{
			destination.accept(buffer, bufferOffset, read);
			total += read;
		}

		if (read < 0 && total == 0)
			return read;

		return total;
	}

	public static int stream(final RandomAccessFile randomAccessFile, final BytesConsumer consumer, final byte[] buffer, final int bufferOffset,
			final int bufferLength) throws IOException
	{
		int read = 0;
		int total = 0;
		while (bufferLength > 0 && (read = randomAccessFile.read(buffer, bufferOffset, bufferLength)) > -1)
		{
			consumer.accept(buffer, bufferOffset, read);
			total += read;
		}

		if (read < 0 && total == 0)
			return read;

		return total;
	}

	public static void streamExact(final int length, final InputStream source, final byte[] destination) throws IOException
	{
		streamExact(length, source, destination, 0);
	}

	public static void streamExact(final int length, final InputStream source, final byte[] destination, final int destinationOffset)
			throws IOException
	{
		streamExact(length, BytesProvider.of(source), destination, destinationOffset);
	}

	public static void streamExact(final int length, final BytesProvider source, final byte[] destination, final int destinationOffset)
			throws IOException
	{
		if (destination.length - destinationOffset < length)
			throw new IOException("Not enough space in destination (buffer overflow)");

		if (length <= 0)
			return;

		int read;
		int total = 0;
		while (total < length)
		{
			read = stream(source, destination, destinationOffset + total, length);

			if (read < 0)
				break;

			total += read;
		}

		if (total != length)
			throw new IOException("Length mismatch (expected " + length + " but read " + total + ")");
	}

	public static void streamExact(final int length, final InputStream source, final OutputStream destination, final byte[] buffer) throws IOException
	{
		streamExact(length, source, destination, buffer, 0, buffer.length);
	}

	public static void streamExact(final int length, final InputStream source, final OutputStream destination, final byte[] buffer,
			final int bufferOffset, final int bufferLength) throws IOException
	{
		streamExact(length, source, BytesConsumer.of(destination), buffer, bufferOffset, bufferLength);
	}

	public static void streamExact(final int length, final InputStream source, final BytesConsumer consumer, final byte[] buffer) throws IOException
	{
		streamExact(length, source, consumer, buffer, 0, buffer.length);
	}

	public static void streamExact(final int length, final InputStream source, final BytesConsumer consumer, final byte[] buffer,
			final int bufferOffset, final int bufferLength) throws IOException
	{
		if (bufferLength == 0)
			throw new IllegalArgumentException("Buffer length must be greater than 0");


		int read;
		int total = 0;
		while (total < length)
		{
			read = stream(source, buffer, bufferOffset, Math.min(length - total, bufferLength));

			if (read == 0 && source.available() <= 0)
			{
				read = source.read();// block until data is available
				if (read < 0)
					throw new IOException("End of stream before target length was reached");

				consumer.accept(read);
				total++;
				continue;
			}

			consumer.accept(buffer, bufferOffset, read);
			total += read;
		}

		if (total != length)
			throw new IOException("Length mismatch (expected " + length + " but read " + total + ")");
	}

	public static void skipExact(final InputStream source, final long length) throws IOException
	{
		long remaining = length;
		long skept;

		while (remaining > 0)
		{
			skept = source.skip(remaining);
			if (skept == 0 && source.available() <= 0)
			{
				skept = source.read();// block until data is available
				if (skept < 0)
					throw new EOFException("End of stream before skip length was reached");
				skept = 1;
			}

			remaining -= skept;
		}
	}

	public static void skipExact(final RandomAccessFile raf, final long length) throws IOException
	{
		long remaining = length;
		int skipped;

		while (remaining > 0)
		{
			skipped = raf.skipBytes((int) Math.min(MAX_INT, remaining));
			if (skipped == 0 && raf.getFilePointer() >= raf.length())
				throw new EOFException("End of file before skip length was reached");

			remaining -= skipped;
		}
	}
}
