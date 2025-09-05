package io.ortis.jsak.lang;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A better implementation than {@link Optional}
 *
 * @param <T>
 */
public class Discretionary<T>
{
	private static final Discretionary<?> ABSENT = new Discretionary<>(false, null);

	private static final IllegalStateException VALUE_NOT_PRESENT = new IllegalStateException("Value is not present");

	private final boolean present;
	private final T value;

	private final int hashCode;

	public Discretionary(final T value)
	{
		this(true, value);
	}

	private Discretionary(final boolean present, final T value)
	{
		this.present = present;
		this.value = value;

		this.hashCode = Objects.hash(this.present, this.value);
	}

	public boolean isPresent()
	{
		return this.present;
	}

	public T getValue()
	{
		if (!this.present)
			throw VALUE_NOT_PRESENT;

		return this.value;
	}

	public <E extends Throwable> T getValueIfPresentOrThrow(final Supplier<E> exception) throws E
	{
		if (!this.present)
			throw exception.get();

		return this.value;
	}

	public T getValueIfPresent(final T fallbackValue)
	{
		if (!this.present)
			return fallbackValue;

		return this.value;
	}

	public T computeValueIfNotPresent(final Supplier<T> fallbackValue)
	{
		return this.present ? this.value : fallbackValue.get();
	}


	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o instanceof final Discretionary<?> other)
			return this.present == other.present && Objects.equals(this.value, other.value);

		return false;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append("{");
		sb.append("present=").append(this.present);

		if (this.present)
			sb.append(", value=").append(this.value);

		sb.append("}");
		return sb.toString();
	}

	public static <T> Discretionary<T> of(final T value)
	{
		return new Discretionary<>(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> Discretionary<T> absent()
	{
		return (Discretionary<T>) ABSENT;
	}
}
