package io.ortis.jsak.lang;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public interface CharIdEnum {

	int ID_STRING_LENGTH = 1;

	char enumCharId();

	default String enumCharIdAsString() {
		return String.valueOf(enumCharId());
	}

	static <T extends CharIdEnum> void check(final Class<T> clazz) {

		final CharIdEnum[] values;
		try {
			final Method method = clazz.getMethod("values");
			values = (CharIdEnum[]) method.invoke(null);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to get values from %s".formatted(clazz), e);
		}

		final List<Character> ids = new LinkedList<>();
		for (final var o : values)
			if (ids.contains(o.enumCharId())) {
				throw new RuntimeException("Duplicate id %s on %s".formatted(o.enumCharId(), clazz));
			} else
				ids.add(o.enumCharId());
	}
}