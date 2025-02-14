package io.ortis.jsak.lang;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public interface IntIdEnum
{
	int enumIntId();

	default String enumIntIdAsString()
	{
		return String.valueOf(enumIntId());
	}

	static <T extends IntIdEnum> void check(final Class<T> clazz)
	{
		check(clazz, null, null);
	}

	static <T extends IntIdEnum> void check(final Class<T> clazz, final Integer minInclusive, final Integer maxInclusive)
	{

		final IntIdEnum[] values;
		try
		{
			final Method method = clazz.getMethod("values");
			values = (IntIdEnum[]) method.invoke(null);
		} catch (final Exception e)
		{
			throw new RuntimeException("Failed to get values from %s".formatted(clazz), e);
		}

		final List<Integer> ids = new LinkedList<>();
		for (final var o : values)
		{
			if (ids.contains(o.enumIntId()))
				throw new RuntimeException("Duplicate id %s on %s".formatted(o.enumIntId(), clazz));
			if ((minInclusive != null && o.enumIntId() < minInclusive) || (maxInclusive != null && o.enumIntId() > maxInclusive))
				throw new RuntimeException("Invalid id %s on %s (out of range [%s, %s])".formatted(o.enumIntId(), clazz, minInclusive, maxInclusive));
			ids.add(o.enumIntId());
		}
	}
}