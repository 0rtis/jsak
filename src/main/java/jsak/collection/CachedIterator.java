package jsak.collection;

import java.util.Iterator;

/**
 * Keep a reference to the current element
 * @param <E>
 */
public class CachedIterator<E> implements Iterator<E>
{
	private final Iterator<E> iterator;
	private int index = -1;
	private E current;

	public CachedIterator(final Iterator<E> iterator)
	{
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	@Override
	public E next()
	{
		this.current = this.iterator.next();
		this.index++;
		return this.current;
	}

	public E getCurrent()
	{
		return this.current;
	}

	public int getIndex()
	{
		return this.index;
	}


	public static <E> CachedIterator<E> of(final Iterable<E> iterable)
	{
		return of(iterable.iterator());
	}

	public static <E> CachedIterator<E> of(final Iterator<E> iterator)
	{
		return new CachedIterator<>(iterator);
	}
}
