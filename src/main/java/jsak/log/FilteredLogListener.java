package jsak.log;


public class FilteredLogListener implements LogService.Listener
{
	public static final FilteredLogListener CONSOLE_ALL = new FilteredLogListener(LogOutput.SYSTEM_OUTPUT_STREAM);
	public static final FilteredLogListener CONSOLE_SEVERE = new FilteredLogListener(LogFilter.SEVERE, LogOutput.SYSTEM_OUTPUT_STREAM);
	public static final FilteredLogListener CONSOLE_INFO = new FilteredLogListener(LogFilter.INFO, LogOutput.SYSTEM_OUTPUT_STREAM);
	public static final FilteredLogListener CONSOLE_FINE = new FilteredLogListener(LogFilter.FINE, LogOutput.SYSTEM_OUTPUT_STREAM);
	public static final FilteredLogListener CONSOLE_FINER = new FilteredLogListener(LogFilter.FINER, LogOutput.SYSTEM_OUTPUT_STREAM);
	public static final FilteredLogListener CONSOLE_FINEST = new FilteredLogListener(LogFilter.FINEST, LogOutput.SYSTEM_OUTPUT_STREAM);

	private final LogFilter filter;
	private final LogOutput output;

	public FilteredLogListener(final LogOutput output)
	{
		this(null, output);
	}

	public FilteredLogListener(final LogFilter filter, final LogOutput output)
	{
		this.filter = filter;
		this.output = output;
	}

	@Override
	public void onEvent(final LogService.Event event)
	{
		if(this.filter == null || this.filter.accept(event.getLoggerName(), event.getLogLevel()))
			this.output.write(event.getFormattedMessage());
	}
}
