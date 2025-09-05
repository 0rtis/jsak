package io.ortis.jsak.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class InputValidator
{

	private InputValidator()
	{
	}

	public static void check(final boolean value, final String message)
	{
		if (!value)
			throw new IllegalArgumentException(message);
	}

	public static <T> T checkNotNull(final T t, final String name)
	{
		return checkNotNull(t, () -> new IllegalArgumentException("'%s' is null".formatted(name)));
	}

	public static <T, E extends Exception> T checkNotNull(final T t, final Supplier<E> exceptionSupplier) throws E
	{
		if (t == null)
			throw exceptionSupplier.get();
		return t;
	}

	public static String checkNotBlank(final String string, final String name)
	{
		if (string == null)
			throw new IllegalArgumentException("'%s' is null".formatted(name));
		if (string.isBlank())
			throw new IllegalArgumentException("'%s' is blank".formatted(name));
		return string;
	}

	public static <E extends Exception> String checkNotBlank(final String string, final Supplier<E> exceptionSupplier) throws E
	{
		if (string == null || string.isBlank())
			throw exceptionSupplier.get();
		return string;
	}

	public static <E extends Exception> String checkNotBlank(final String string, final String prefix, final Supplier<E> exceptionSupplier) throws E
	{
		if (!checkNotBlank(string, exceptionSupplier).startsWith(prefix))
			throw exceptionSupplier.get();
		return string;
	}

	public static String checkOptionalNotBlank(final String string, final String name)
	{
		return checkOptionalNotBlank(string, () -> new IllegalArgumentException("'%s' is blank".formatted(name)));
	}

	public static <E extends Exception> String checkOptionalNotBlank(final String string, final Supplier<E> exceptionSupplier) throws E
	{
		if (string == null)
			return null;
		if (string.isBlank())
			throw exceptionSupplier.get();
		return string;
	}


	public static String checkLength(final String string, final int length, final String name)
	{
		return checkLength(string, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <E extends Exception> String checkLength(final String string, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (string == null || string.length() != length)
			throw exceptionSupplier.get();
		return string;
	}

	public static String checkPrefix(final String string, final String prefix, final String name)
	{
		return checkPrefix(string, prefix, () -> new IllegalArgumentException("'%s' does not start with prefix '%s'".formatted(name, prefix)));
	}

	public static <E extends Exception> String checkPrefix(final String string, final String prefix, final Supplier<E> exceptionSupplier) throws E
	{
		if (!checkNotBlank(string, exceptionSupplier).startsWith(prefix))
			throw exceptionSupplier.get();
		return string;
	}

	public static BigInteger checkInteger(final String string, final String name)
	{
		return checkInteger(string, 10, name);
	}

	public static BigInteger checkInteger(final String string, final int radix, final String name)
	{
		try
		{
			return new BigInteger(string, radix);
		} catch (final NumberFormatException e)
		{
			throw new IllegalArgumentException("'%s' is not a valid integer".formatted(name));
		}
	}


	public static <E extends Exception> BigInteger checkInteger(final String string, final Supplier<E> exceptionSupplier) throws E
	{
		return checkInteger(string, 10, exceptionSupplier);
	}

	public static <E extends Exception> BigInteger checkInteger(final String string, final int radix, final Supplier<E> exceptionSupplier) throws E
	{
		try
		{
			return new BigInteger(string, radix);
		} catch (final NumberFormatException e)
		{
			throw exceptionSupplier.get();
		}
	}

	public static long checkEqual(final long value, final long expectedValue, final String name)
	{
		return checkEqual(value, expectedValue, () -> new IllegalArgumentException("%s is different from %s".formatted(name, value)));
	}

	public static <E extends Exception> long checkEqual(final long value, final long expectedValue, final Supplier<E> exceptionSupplier) throws E
	{
		if (value != expectedValue)
			throw exceptionSupplier.get();
		return value;
	}

	public static <T> T checkEqual(final T value, final T expectedValue, final String name)
	{
		return checkEqual(value, expectedValue, () -> new IllegalArgumentException("%s is different from %s".formatted(name, value)));
	}

	public static <T, E extends Exception> T checkEqual(final T value, final T expectedValue, final Supplier<E> exceptionSupplier) throws E
	{
		if (!Objects.equals(value, expectedValue))
			throw exceptionSupplier.get();
		return value;
	}

	public static int checkNonNegative(final int value, final String name)
	{
		return checkNonNegative(value, () -> new IllegalArgumentException("'%s' is less than 0: %s".formatted(name, value)));
	}

	public static <E extends Exception> int checkNonNegative(final int value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value < 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static long checkNonNegative(final long value, final String name)
	{
		return checkNonNegative(value, () -> new IllegalArgumentException("'%s' is less than 0: %s".formatted(name, value)));
	}

	public static <E extends Exception> long checkNonNegative(final long value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value < 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static BigInteger checkNonNegative(final BigInteger value, final String name)
	{
		if (value == null)
			throw new IllegalArgumentException("'%s' is null".formatted(name));
		if (value.signum() < 0)
			throw new IllegalArgumentException("%s is less than 0: %s".formatted(name, value));
		return value;
	}

	public static <E extends Exception> BigInteger checkNonNegative(final BigInteger value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value == null || value.signum() < 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static double checkNonNegative(final double value, final String name)
	{
		return checkNonNegative(value, () -> new IllegalArgumentException("'%s' is less than 0: %s".formatted(name, value)));
	}

	public static <E extends Exception> double checkNonNegative(final double value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value < 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static BigDecimal checkNonNegative(final BigDecimal value, final String name)
	{
		if (value == null)
			throw new IllegalArgumentException("'%s' is null".formatted(name));
		if (value.signum() < 0)
			throw new IllegalArgumentException("%s is less than 0: %s".formatted(name, value));
		return value;
	}

	public static <E extends Exception> BigDecimal checkNonNegative(final BigDecimal value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value == null || value.signum() < 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static Duration checkNonNegative(final Duration value, final String name)
	{
		if (value == null)
			throw new IllegalArgumentException("'%s' is null".formatted(name));
		if (value.isNegative())
			throw new IllegalArgumentException("%s is less than 0: %s".formatted(name, value));
		return value;
	}

	public static <E extends Exception> Duration checkNonNegative(final Duration value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value == null || value.isNegative())
			throw exceptionSupplier.get();
		return value;
	}

	public static int checkStrictlyPositive(final int value, final String name)
	{
		return checkStrictlyPositive(value, () -> new IllegalArgumentException("'%s' is less than 1: %s".formatted(name, value)));
	}

	public static <E extends Exception> int checkStrictlyPositive(final int value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value <= 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static long checkStrictlyPositive(final long value, final String name)
	{
		return checkStrictlyPositive(value, () -> new IllegalArgumentException("'%s' is less than 1: %s".formatted(name, value)));
	}

	public static <E extends Exception> long checkStrictlyPositive(final long value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value <= 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static BigInteger checkStrictlyPositive(final BigInteger value, final String name)
	{
		if (value == null)
			throw new IllegalArgumentException("'%s' is null".formatted(name));
		if (value.signum() <= 0)
			throw new IllegalArgumentException("'%s' is less than 1: %s".formatted(name, value));
		return value;
	}

	public static <E extends Exception> BigInteger checkStrictlyPositive(final BigInteger value, final Supplier<E> exceptionSupplier) throws E
	{
		if (value == null || value.signum() <= 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static int checkRange(final int value, final int includedLowerBound, final int excludedUpperBound, final String name)
	{
		return checkRange(value, includedLowerBound, excludedUpperBound,
				() -> new IllegalArgumentException("'%s' is out of range [%s, %s): %s".formatted(name, includedLowerBound, excludedUpperBound, value))
		);
	}

	public static <E extends Exception> int checkRange(final int value, final int includedLowerBound, final int excludedUpperBound,
			final Supplier<E> exceptionSupplier) throws E
	{
		if (includedLowerBound >= excludedUpperBound)
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(includedLowerBound, excludedUpperBound));
		if (value < includedLowerBound || value >= excludedUpperBound)
			throw exceptionSupplier.get();
		return value;
	}

	public static long checkRange(final long value, final long includedLowerBound, final long excludedUpperBound, final String name)
	{
		return checkRange(value, includedLowerBound, excludedUpperBound,
				() -> new IllegalArgumentException("'%s' is out of range [%s, %s): %s".formatted(name, includedLowerBound, excludedUpperBound, value))
		);
	}

	public static <E extends Exception> long checkRange(final long value, final long includedLowerBound, final long excludedUpperBound,
			final Supplier<E> exceptionSupplier) throws E
	{
		if (includedLowerBound >= excludedUpperBound)
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(includedLowerBound, excludedUpperBound));
		if (value < includedLowerBound || value >= excludedUpperBound)
			throw exceptionSupplier.get();
		return value;
	}

	public static BigInteger checkRange(final BigInteger value, final BigInteger includedLowerBound, final BigInteger excludedUpperBound,
			final String name)
	{
		if (includedLowerBound.compareTo(excludedUpperBound) >= 0)
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(includedLowerBound, excludedUpperBound));
		if (value == null)
			throw new IllegalArgumentException("'%s' is null".formatted(name));
		if (value.compareTo(includedLowerBound) < 0 || value.compareTo(excludedUpperBound) >= 0)
			throw new IllegalArgumentException("'%s' is out of range [%s, %s): %s".formatted(name, includedLowerBound, excludedUpperBound, value));
		return value;
	}

	public static <E extends Exception> BigInteger checkRange(final BigInteger value, final BigInteger includedLowerBound,
			final BigInteger excludedUpperBound,
			final Supplier<E> exceptionSupplier) throws E
	{
		if (includedLowerBound.compareTo(excludedUpperBound) >= 0)
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(includedLowerBound, excludedUpperBound));

		if (value == null || value.compareTo(includedLowerBound) < 0 || value.compareTo(excludedUpperBound) >= 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static String checkLengthRange(final String string, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(string, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, string == null ? null : string.length()))
		);
	}

	public static <E extends Exception> String checkLengthRange(final String string, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (string == null
			|| (minLengthIncluded >= 0 && string.length() < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && string.length() >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return string;
	}


	public static boolean[] checkLengthRange(final boolean[] array, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(array, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, array == null ? null : array.length)));
	}

	public static <E extends Exception> boolean[] checkLengthRange(final boolean[] array, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (array == null
			|| (minLengthIncluded >= 0 && array.length < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && array.length >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return array;
	}


	public static byte[] checkLengthRange(final byte[] array, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(array, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, array == null ? null : array.length)));
	}

	public static <E extends Exception> byte[] checkLengthRange(final byte[] array, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (array == null
			|| (minLengthIncluded >= 0 && array.length < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && array.length >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return array;
	}

	public static short[] checkLengthRange(final short[] array, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(array, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, array == null ? null : array.length)));
	}

	public static <E extends Exception> short[] checkLengthRange(final short[] array, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (array == null
			|| (minLengthIncluded >= 0 && array.length < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && array.length >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return array;
	}

	public static int[] checkLengthRange(final int[] array, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(array, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, array == null ? null : array.length)));
	}

	public static <E extends Exception> int[] checkLengthRange(final int[] array, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (array == null
			|| (minLengthIncluded >= 0 && array.length < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && array.length >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return array;
	}

	public static long[] checkLengthRange(final long[] array, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(array, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, array == null ? null : array.length)));
	}

	public static <E extends Exception> long[] checkLengthRange(final long[] array, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (array == null
			|| (minLengthIncluded >= 0 && array.length < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && array.length >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return array;
	}

	public static <T> T[] checkLengthRange(final T[] array, final int minLengthIncluded, final int maxLengthExcluded, final String name)
	{
		return checkLengthRange(array, minLengthIncluded, maxLengthExcluded, () -> new IllegalArgumentException(
				"'%s' length is out of range [%s, %s): %s".formatted(name, minLengthIncluded, maxLengthExcluded, array == null ? null : array.length)));
	}

	public static <T, E extends Exception> T[] checkLengthRange(final T[] array, final int minLengthIncluded, final int maxLengthExcluded,
			final Supplier<E> exceptionSupplier) throws E
	{
		if ((minLengthIncluded < 0 && maxLengthExcluded < 0) ||
			(minLengthIncluded >= 0 && maxLengthExcluded >= 0 && minLengthIncluded >= maxLengthExcluded))
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(minLengthIncluded, maxLengthExcluded));
		if (array == null
			|| (minLengthIncluded >= 0 && array.length < minLengthIncluded)
			|| (maxLengthExcluded >= 0 && array.length >= maxLengthExcluded))
			throw exceptionSupplier.get();

		return array;
	}


	public static boolean[] checkLength(final boolean[] array, final int length, final String name)
	{
		return checkLength(array, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <E extends Exception> boolean[] checkLength(final boolean[] array, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (array == null || array.length != length)
			throw exceptionSupplier.get();
		return array;
	}


	public static byte[] checkLength(final byte[] array, final int length, final String name)
	{
		return checkLength(array, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <E extends Exception> byte[] checkLength(final byte[] array, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (array == null || array.length != length)
			throw exceptionSupplier.get();
		return array;
	}

	public static short[] checkLength(final short[] array, final int length, final String name)
	{
		return checkLength(array, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <E extends Exception> short[] checkLength(final short[] array, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (array == null || array.length != length)
			throw exceptionSupplier.get();
		return array;
	}

	public static int[] checkLength(final int[] array, final int length, final String name)
	{
		return checkLength(array, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <E extends Exception> int[] checkLength(final int[] array, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (array == null || array.length != length)
			throw exceptionSupplier.get();
		return array;
	}

	public static long[] checkLength(final long[] array, final int length, final String name)
	{
		return checkLength(array, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <E extends Exception> long[] checkLength(final long[] array, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (array == null || array.length != length)
			throw exceptionSupplier.get();
		return array;
	}

	public static <T> T[] checkLength(final T[] array, final int length, final String name)
	{
		return checkLength(array, length, () -> new IllegalArgumentException("'%s' length is not %s".formatted(name, length)));
	}

	public static <T, E extends Exception> T[] checkLength(final T[] array, final int length, final Supplier<E> exceptionSupplier) throws E
	{
		if (array == null || array.length != length)
			throw exceptionSupplier.get();
		return array;
	}


	public static Duration checkDuration(final Duration value, final long includedLowerBoundMillis, final long excludedUpperBoundMillis,
			final String name)
	{
		return checkDuration(value, includedLowerBoundMillis, excludedUpperBoundMillis, () -> new IllegalArgumentException(
				"'%s' is out of range [%s, %s): %s".formatted(name, Duration.ofMillis(includedLowerBoundMillis),
						Duration.ofMillis(excludedUpperBoundMillis), value)));
	}

	public static <E extends Exception> Duration checkDuration(final Duration value, final long includedLowerBoundMillis,
			final long excludedUpperBoundMillis, final Supplier<E> exceptionSupplier) throws E
	{
		return checkDuration(value, Duration.ofMillis(includedLowerBoundMillis), Duration.ofMillis(excludedUpperBoundMillis), exceptionSupplier);
	}

	public static Duration checkDuration(final Duration value, final Duration includedLowerBound, final Duration excludedUpperBound,
			final String name)
	{
		return checkDuration(value, includedLowerBound, excludedUpperBound, () -> new IllegalArgumentException(
				"'%s' is out of range [%s, %s): %s".formatted(name, includedLowerBound, excludedUpperBound, value)));
	}

	public static <E extends Exception> Duration checkDuration(final Duration value, final Duration includedLowerBound,
			final Duration excludedUpperBound, final Supplier<E> exceptionSupplier) throws E
	{
		if (includedLowerBound.compareTo(excludedUpperBound) >= 0)
			throw new IllegalArgumentException("Invalid range [%s, %s)".formatted(includedLowerBound, excludedUpperBound));

		if (value == null || value.compareTo(includedLowerBound) < 0 || value.compareTo(excludedUpperBound) >= 0)
			throw exceptionSupplier.get();
		return value;
	}

	public static <C> C checkInstanceOf(final Object object, final Class<C> clazz, final String name)
	{
		if (!clazz.isInstance(object))
			throw new IllegalArgumentException("'%s' is not an instance of %s".formatted(name, clazz.getName()));
		return clazz.cast(object);
	}

	public static <C, E extends Exception> C checkInstanceOf(final Object object, final Class<C> clazz, final Supplier<E> exceptionSupplier) throws E
	{
		if (!clazz.isInstance(object))
			throw exceptionSupplier.get();
		return clazz.cast(object);
	}

	public static <T> List<T> checkNotNullAndCopy(final List<T> list, final String name)
	{
		return List.copyOf(InputValidator.checkNotNull(list, name));
	}

	public static <T> List<T> copyAnCheckNotEmpty(final List<T> list, final String name)
	{
		final List<T> copy = List.copyOf(InputValidator.checkNotNull(list, name));
		checkStrictlyPositive(copy.size(), "%s.%s".formatted(name, "size"));
		return copy;
	}


	public static <T, E extends Exception> List<T> copyAnCheckNotEmpty(final List<T> list, final Supplier<E> exceptionSupplier) throws E
	{
		if (list == null)
			throw exceptionSupplier.get();
		final List<T> copy = List.copyOf(list);
		checkStrictlyPositive(copy.size(), exceptionSupplier);
		return copy;
	}

}