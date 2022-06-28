package io.ortis.jsak.collection;

import io.ortis.jsak.TestUtils;
import io.ortis.jsak.io.IOUtils;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class CachedIteratorTest
{

	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

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
			Assert.assertEquals(value, cachedIterator.next());
			Assert.assertEquals(index++, cachedIterator.getIndex());
			Assert.assertEquals(value, cachedIterator.getCurrent());
		}
	}


	@BeforeClass
	public static void setUpBeforeClass()
	{

	}

	@AfterClass
	public static void tearDownAfterClass()
	{

	}
}
