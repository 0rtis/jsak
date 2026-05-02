package io.ortis.jsak.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ObjectEncoder
{

	private static final String DEFAULT_LIST_DELIMITER = ",";

	private static final Supplier<IllegalArgumentException> FAILED_TO_PARSE_BOOLEAN = () -> new IllegalArgumentException("Failed to parse boolean");
	private static final Supplier<IllegalArgumentException> FAILED_TO_PARSE_LONG = () -> new IllegalArgumentException("Failed to parse long");
	private static final Supplier<IllegalArgumentException> FAILED_TO_PARSE_LIST = () -> new IllegalArgumentException("Failed to parse list");

	private ObjectEncoder()
	{
	}

	// from string

	public static String fromStrings(final String... values)
	{
		return fromIterableOfString(List.of(values));
	}

	public static String fromListOfStrings(final List<String> values)
	{
		return fromIterableOfString(values);
	}

	public static String fromStreamOfString(final Stream<String> value)
	{
		return fromStreamOfString(value, DEFAULT_LIST_DELIMITER);
	}

	public static String fromStreamOfString(final Stream<String> value, final String delimiter)
	{
		return fromIterableOfString(value.toList(), delimiter);
	}


	public static String fromIterableOfString(final Iterable<String> value)
	{
		return fromIterableOfString(value, DEFAULT_LIST_DELIMITER);
	}

	public static String fromIterableOfString(final Iterable<String> value, final String delimiter)
	{
		return String.join(delimiter, value);
	}


	// Boolean

	public static boolean toBoolean(final String value)
	{
		return toBoolean(value, FAILED_TO_PARSE_BOOLEAN);
	}


	public static <E extends Exception> boolean toBoolean(final String value, final Supplier<E> parsingFailedException) throws E
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


	// Long

	public static <E extends Exception> long toLong(final String value) throws E
	{
		return toLong(value, FAILED_TO_PARSE_LONG);
	}

	public static <E extends Exception> long toLong(final String value, final Supplier<E> parsingFailedException) throws E
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


	// List of String


	public static <E extends Exception> List<String> toListOfString(final String value) throws E
	{
		return toListOfString(value, FAILED_TO_PARSE_LIST);
	}

	public static <E extends Exception> List<String> toListOfString(final String value, final String regexDelimiter) throws E
	{
		return toListOfString(value, regexDelimiter, FAILED_TO_PARSE_LIST);
	}

	public static <E extends Exception> List<String> toListOfString(final String value, final Supplier<E> parsingFailedException) throws E
	{
		return toListOfString(value, DEFAULT_LIST_DELIMITER, parsingFailedException);
	}

	public static <E extends Exception> List<String> toListOfString(final String value, final String regexDelimiter,
			final Supplier<E> parsingFailedException) throws E
	{
		try
		{
			return Stream.of(value.split(regexDelimiter)).toList();
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}

	// Stream of String

	public static <E extends Exception> Stream<String> toStreamOfString(final String value) throws E
	{
		return toStreamOfString(value, FAILED_TO_PARSE_LIST);
	}

	public static <E extends Exception> Stream<String> toStreamOfString(final String value, final Supplier<E> parsingFailedException) throws E
	{
		return toStreamOfString(value, DEFAULT_LIST_DELIMITER, parsingFailedException);
	}

	public static <E extends Exception> Stream<String> toStreamOfString(final String value, final String regexDelimiter,
			final Supplier<E> parsingFailedException) throws E
	{
		try
		{
			return Stream.of(value.split(regexDelimiter)).map(String::trim);
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}

	// List of Long

	public static <E extends Exception> List<Long> toListOfLong(final String value, final String regexDelimiter) throws E
	{
		return toListOfLong(value, regexDelimiter, FAILED_TO_PARSE_LIST);

	}

	public static <E extends Exception> List<Long> toListOfLong(final String value, final Supplier<E> parsingFailedException) throws E
	{
		return toListOfLong(value, DEFAULT_LIST_DELIMITER, parsingFailedException);

	}

	public static <E extends Exception> List<Long> toListOfLong(final String value, final String regexDelimiter,
			final Supplier<E> parsingFailedException) throws E
	{
		try
		{
			return Stream.of(value.split(regexDelimiter)).map(String::trim).map(Long::parseLong).toList();
		} catch (final Throwable t)
		{
			throw parsingFailedException.get();
		}
	}


}
