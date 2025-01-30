package io.ortis.jsak.lang;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public interface StringIdEnum
{

	String enumStringId();

	static <T extends StringIdEnum> void check(final Class<T> clazz)
	{
		check(clazz, -1);
	}

	static <T extends StringIdEnum> void check(final Class<T> clazz, final int length)
	{

		final StringIdEnum[] values;
		try
		{
			final Method method = clazz.getMethod("values");
			values = (StringIdEnum[]) method.invoke(null);
		} catch (final Exception e)
		{
			throw new RuntimeException("Failed to get values from %s".formatted(clazz), e);
		}

		final List<String> ids = new LinkedList<>();
		for (final var o : values)
			if (length > -1 && o.enumStringId().length() != length)
				throw new RuntimeException("Invalid length of %s on %s".formatted(o.enumStringId(), clazz));
			else if (ids.contains(o.enumStringId()))
				throw new RuntimeException("Duplicate id %s on %s".formatted(o.enumStringId(), clazz));
			else
				ids.add(o.enumStringId());
	}
}