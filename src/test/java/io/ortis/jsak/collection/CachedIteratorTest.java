package io.ortis.jsak.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CachedIteratorTest
{

	@Test
	public void test() throws Exception
	{
		final List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 256; i++)
			list.add(i);

		final Iterator<Integer> iterator = list.iterator();
		final CachedIterator<Integer> cachedIterator = CachedIterator.of(list);

		int index = 0;
		while (iterator.hasNext())
		{
			final Integer value = iterator.next();
			Assertions.assertEquals(value, cachedIterator.next());
			Assertions.assertEquals(index++, cachedIterator.getIndex());
			Assertions.assertEquals(value, cachedIterator.getCurrent());
		}
	}

}
