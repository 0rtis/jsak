package io.ortis.jsak.log.output;

import io.ortis.jsak.log.FilteredLogListener;
import io.ortis.jsak.log.LogService;

public class Console implements LogService.Listener
{
	@Override
	public void onEvent(final LogService.Event event)
	{
		FilteredLogListener.CONSOLE_ALL.onEvent(event);
	}

	@Override
	public int hashCode()
	{
		return 0;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final Console other = (Console) o;
		return true;
	}
}
