package io.ortis.jsak.http.server.limiter;

import java.time.Duration;
import java.util.*;

public class TimeHTTPLimiter implements HTTPLimiter
{

	private final int strike;
	private final long timeframe;

	private final Map<String, Record> records = new HashMap<>();


	public TimeHTTPLimiter(final int strike, final Duration timeframe)
	{
		this.strike = strike;
		this.timeframe = timeframe.toMillis();
		if (this.timeframe <= 0)
			throw new IllegalStateException("Time frame must be greater than 0");

	}

	@Override
	public String onRequest(final String host, final long now)
	{
		final Record record;
		synchronized (this.records)
		{
			record = this.records.computeIfAbsent(host, Record::new);

			final long after = now - this.timeframe;
			final Long first = record.checkAndClean(after, this.strike);
			if (first != null)
				return "Too many requests (wait " + ((int) ((first - after) / 1000)) + " seconds)";

			record.add(now);
		}

		return null;
	}

	@Override
	public void clean(final long now)
	{
		synchronized (this.records)
		{
			final List<Record> hosts = new ArrayList<>(this.records.values());
			this.records.clear();
			for (final Record record : hosts)
			{
				record.clean(now - this.timeframe);
				if (record.size() > 0)
					this.records.put(record.getHost(), record);
			}
		}
	}

	private static class Record
	{
		private final String host;
		private final List<Long> timestamps = new LinkedList<>();

		public Record(final String host)
		{
			this.host = host;
		}

		public Long checkAndClean(final long after, final int maxStrikes)
		{
			final ListIterator<Long> iterator = this.timestamps.listIterator();

			Long first = null;
			int count = 0;
			while (iterator.hasNext())
			{
				final long t = iterator.next();
				if (t > after)
				{
					if (first == null || first > t)
						first = t;
					count++;
				} else
					iterator.remove();
			}

			return count >= maxStrikes ? first : null;
		}

		public String getHost()
		{
			return this.host;
		}

		public void add(final Long now)
		{
			this.timestamps.add(now);
		}

		public void clean(final Long min)
		{
			this.timestamps.removeIf(t -> t < min);
		}

		public int size()
		{
			return this.timestamps.size();
		}

		public List<Long> getTimestamps()
		{
			return timestamps;
		}
	}
}

