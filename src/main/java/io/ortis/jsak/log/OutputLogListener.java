package io.ortis.jsak.log;


public class OutputLogListener implements LogService.Listener
{
	private final LogOutput output;

	public OutputLogListener(final LogOutput output)
	{
		this.output = output;
	}

	@Override
	public void onEvent(final LogService.Event event)
	{
		this.output.write(event.getFormattedMessage());
	}

	@Override
	public int hashCode()
	{
		return this.output.hashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final OutputLogListener other = (OutputLogListener) o;
		return this.output.equals(other.output);
	}
}
