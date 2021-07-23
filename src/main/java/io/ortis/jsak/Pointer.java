package io.ortis.jsak;

/**
 * Reference to an {@link Object}. Useful to access outer field from anonymous class.
 * @param <T>
 */
public class Pointer<T>
{
	private T value;

	public Pointer(final T value)
	{
		this.value = value;
	}

	public T getValue()
	{
		return this.value;
	}

	public void setValue(final T value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{" + "value=" + this.value + "}";
	}

	public static <T> Pointer<T> of(final T value)
	{
		return new Pointer<>(value);
	}
}
