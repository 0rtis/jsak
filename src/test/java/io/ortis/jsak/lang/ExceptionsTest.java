package io.ortis.jsak.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExceptionsTest
{

	@Test
	void testFind()
	{
		final List<Throwable> exceptions = new LinkedList<>();
		exceptions.add(new IllegalStateException());
		exceptions.add(new IllegalArgumentException(exceptions.getLast()));
		exceptions.add(new IOException(exceptions.getLast()));
		exceptions.add(new Exception(exceptions.getLast()));

		final Throwable root = exceptions.getLast();


		for (int i = 0; i < exceptions.size(); i++)
			Assertions.assertEquals(exceptions.getLast(),
					Exceptions.find(root, Exception.class, i).getValueIfPresent(() -> {
						Assertions.fail();
						return null;
					}));


		for (int i = 0; i <= 0; i++)
			Assertions.assertNull(Exceptions.find(root, IOException.class, 0).getValueIfPresent(null));
		for (int i = 1; i < exceptions.size(); i++)
			Assertions.assertEquals(exceptions.get(exceptions.size() - 2),
					Exceptions.find(root, IOException.class, i).getValueIfPresent(() -> {
						Assertions.fail();
						return null;
					}));


		for (int i = 0; i <= 1; i++)
			Assertions.assertNull(Exceptions.find(root, IllegalArgumentException.class, i).getValueIfPresent(null));
		for (int i = 2; i < exceptions.size(); i++)
			Assertions.assertEquals(exceptions.get(exceptions.size() - 3),
					Exceptions.find(root, IllegalArgumentException.class, i).getValueIfPresent(() -> {
						Assertions.fail();
						return null;
					}));

		for (int i = 0; i <= 2; i++)
			Assertions.assertNull(Exceptions.find(root, IllegalStateException.class, i).getValueIfPresent(null));
		for (int i = 3; i < exceptions.size(); i++)
			Assertions.assertEquals(exceptions.get(exceptions.size() - 4),
					Exceptions.find(root, IllegalStateException.class, i).getValueIfPresent(() -> {
						Assertions.fail();
						return null;
					}));


	}
}
