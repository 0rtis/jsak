package jsak.log;

import java.util.logging.Level;

public interface LogFilter
{
	public static final LogFilter SEVERE = (loggerName, logLevel) -> logLevel.intValue() >= Level.SEVERE.intValue();
	public static final LogFilter WARNING = (loggerName, logLevel) -> logLevel.intValue() >= Level.WARNING.intValue();
	public static final LogFilter INFO = (loggerName, logLevel) -> logLevel.intValue() >= Level.INFO.intValue();
	public static final LogFilter FINE = (loggerName, logLevel) -> logLevel.intValue() >= Level.FINE.intValue();
	public static final LogFilter FINER = (loggerName, logLevel) -> logLevel.intValue() >= Level.FINER.intValue();
	public static final LogFilter FINEST = (loggerName, logLevel) -> logLevel.intValue() >= Level.FINEST.intValue();

	public boolean accept(final String loggerName, final Level logLevel);
}
