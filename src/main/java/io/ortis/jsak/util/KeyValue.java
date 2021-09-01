package io.ortis.jsak.util;

import java.util.Objects;

public class KeyValue<K, V>
{
	private final K key;
	private final V value;

	public KeyValue(final K key, final V value)
	{
		this.key = key;
		this.value = value;
	}

	public K getKey()
	{
		return this.key;
	}

	public V getValue()
	{
		return this.value;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.key, this.value);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o instanceof KeyValue)
		{
			final KeyValue<?, ?> other = (KeyValue<?, ?>) o;
			return Objects.equals(this.key, other.key) && Objects.equals(this.value, other.value);
		}
			return false;
	}
}
