package io.ortis.jsak.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression
{
	public enum Algorithm
	{
		Gzip, Raw;

		public static Algorithm from(String algo)
		{
			if (algo == null)
				return null;

			algo = algo.toUpperCase(Locale.ENGLISH);
			for (final Algorithm ca : Algorithm.values())
				if (ca.name().toUpperCase(Locale.ENGLISH).equals(algo))
					return ca;

			return null;
		}
	}

	/**
	 * Compress data
	 *
	 * @return
	 * @throws IOException
	 */
	public static void deflate(final Algorithm algorithm, final InputStream source, final OutputStream destination,
			final byte[] buffer) throws IOException
	{

		final byte[] compressed;

		switch (algorithm)
		{
			case Gzip:
			{
				try (final GZIPOutputStream gos = new GZIPOutputStream(destination))
				{
					IOUtils.stream(source, gos, buffer);
					gos.flush();
				}
			}
			break;

			case Raw:
				IOUtils.stream(source, destination, buffer);
				break;

			default:
				throw new IllegalArgumentException("Unhandled compression " + algorithm);
		}
	}

	/**
	 * Uncompress data
	 *
	 * @return
	 */
	public static void inflate(final Algorithm algorithm, final InputStream source, final byte[] buffer,
			final OutputStream destination) throws IOException
	{
		final byte[] raw;
		switch (algorithm)
		{
			case Gzip:
			{
				try (final GZIPInputStream gis = new GZIPInputStream(source))
				{
					IOUtils.stream(gis, destination, buffer);
				}
			}
			break;

			case Raw:
				IOUtils.stream(source, destination, buffer);
				break;

			default:
				throw new IllegalArgumentException("Unhandled compression " + algorithm);
		}
	}
}
