package io.ortis.jsak.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class InputParser
{

	private static final Supplier<IllegalArgumentException> FAILED_TO_PARSE_BOOLEAN = () -> new IllegalArgumentException("Failed to parse boolean");
	private static final Supplier<IllegalArgumentException> FAILED_TO_PARSE_LONG = () -> new IllegalArgumentException("Failed to parse long");
	private static final Supplier<IllegalArgumentException> FAILED_TO_PARSE_LIST = () -> new IllegalArgumentException("Failed to parse list");

	private InputParser()
	{
	}

	public static boolean parseBoolean(final String value)
	{
		return parseBoolean(value, FAILED_TO_PARSE_BOOLEAN);
	}


	public static <E extends Exception> boolean parseBoolean(final String value, final Supplier<E> parsingFailedException) throws E
	{
		try
		{
			if ("true".equalsIgnoreCase(value.trim()))
				return true;
			if ("false".equalsIgnoreCase(value.trim()))
				return false;
			throw parsingFailedException.get();
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}


	public static <E extends Exception> long parseLong(final String value) throws E
	{
		return parseLong(value, FAILED_TO_PARSE_LONG);
	}

	public static <E extends Exception> long parseLong(final String value, final Supplier<E> parsingFailedException) throws E
	{
		try
		{
			final long result = Long.parseLong(value.trim());

			return result;
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}

	public static <E extends Exception> List<String> parseArrayToList(final String value) throws E
	{
		return parseArrayToList(value, FAILED_TO_PARSE_LIST);

	}

	public static <E extends Exception> List<String> parseArrayToList(final String value, final String regexDelimiter) throws E
	{
		return parseArrayToList(value, regexDelimiter, FAILED_TO_PARSE_LIST);

	}

	public static <E extends Exception> List<String> parseArrayToList(final String value, final Supplier<E> parsingFailedException) throws E
	{
		return parseArrayToList(value, ",", parsingFailedException);

	}

	public static <E extends Exception> List<String> parseArrayToList(final String value, final String regexDelimiter,
			final Supplier<E> parsingFailedException)
			throws E
	{
		try
		{
			return Stream.of(value.split(regexDelimiter)).map(String::trim).toList();
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}

	public static <E extends Exception> Stream<String> parseArrayToStream(final String value) throws E
	{
		return parseArrayToStream(value, FAILED_TO_PARSE_LIST);
	}

	public static <E extends Exception> Stream<String> parseArrayToStream(final String value, final Supplier<E> parsingFailedException) throws E
	{
		return parseArrayToStream(value, ",", parsingFailedException);
	}

	public static <E extends Exception> Stream<String> parseArrayToStream(final String value, final String regexDelimiter,
			final Supplier<E> parsingFailedException)
			throws E
	{
		try
		{
			return Stream.of(value.split(regexDelimiter)).map(String::trim);
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}
}
