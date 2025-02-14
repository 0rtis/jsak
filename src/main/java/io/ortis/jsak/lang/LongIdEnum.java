package io.ortis.jsak.lang;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public interface LongIdEnum
{
	long enumLongId();

	default String enumLongIdAsString()
	{
		return String.valueOf(enumLongId());
	}

	static <T extends LongIdEnum> void check(final Class<T> clazz)
	{
		check(clazz, null, null);
	}

	static <T extends LongIdEnum> void check(final Class<T> clazz, final Long minInclusive, final Long maxInclusive)
	{

		final LongIdEnum[] values;
		try
		{
			final Method method = clazz.getMethod("values");
			values = (LongIdEnum[]) method.invoke(null);
		} catch (final Exception e)
		{
			throw new RuntimeException("Failed to get values from %s".formatted(clazz), e);
		}

		final List<Long> ids = new LinkedList<>();
		for (final var o : values)
		{
			if (ids.contains(o.enumLongId()))
				throw new RuntimeException("Duplicate id %s on %s".formatted(o.enumLongId(), clazz));
			if ((minInclusive != null && o.enumLongId() < minInclusive) || (maxInclusive != null && o.enumLongId() > maxInclusive))
				throw new RuntimeException("Invalid id %s on %s (out of range [%s, %s])".formatted(o.enumLongId(), clazz, minInclusive, maxInclusive));
			ids.add(o.enumLongId());
		}
	}
}