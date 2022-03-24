package io.ortis.jsak.log;


import io.ortis.jsak.FormatUtils;
import io.ortis.jsak.log.config.LogServiceConfig;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogService extends Handler implements Runnable
{
	private static final Duration COOLDOWN = Duration.ofSeconds(60);

	private final Map<String, Logger> cache = new HashMap<>();
	private final List<LogService.Listener> listeners = new LinkedList<>();

	private transient LogRecordFormatter recordFormatter = LogRecordFormatter.ONE_LINE_FORMATTER;
	private transient Clock clock = Clock.systemDefaultZone();
	private transient final PriorityBlockingQueue<LogService.Event> eventQueue = new PriorityBlockingQueue<>();
	private transient final Object lock = new Object();

	@Override
	public void run()
	{
		try
		{
			while (!Thread.interrupted())
			{
				try
				{

					final LogService.Event event = this.eventQueue.poll(1000, TimeUnit.MILLISECONDS);

					if (event != null && event.getLogLevel().intValue() >= getLevel().intValue())
					{
						synchronized (this.listeners)
						{
							for (final LogService.Listener listener : this.listeners)
								listener.onEvent(event);
						}
					}

				} catch (final InterruptedException e)
				{
					throw e;
				} catch (final Exception e)
				{
					System.out.println("Error while processing log events - " + e.getMessage());
					e.printStackTrace();
					Thread.sleep(COOLDOWN.toMillis());
				}
			}

		} catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch (final Exception e)
		{
			System.out.println("Fatal error in " + getClass().getSimpleName() + " - " + e.getMessage());
			e.printStackTrace();
		}
	}


	public LogService defaultListener()
	{
		addListener(event -> System.out.println(event.getFormattedMessage()));
		return this;
	}

	public LogService start()
	{
		final Thread thread = new Thread(this);
		thread.setName(getClass().getSimpleName());
		thread.setDaemon(true);
		thread.start();
		return this;
	}

	@Override
	public void publish(final LogRecord record)
	{
		final LogService.Event event;
		synchronized (this.lock)
		{
			final LocalDateTime now = LocalDateTime.now(this.clock);
			final String formattedMessage = this.recordFormatter.format(now, record);
			event = new Event(now, record, formattedMessage);
		}
		this.eventQueue.add(event);
	}

	@Override
	public void flush()
	{

	}

	@Override
	public void close() throws SecurityException
	{

	}

	public Logger getLogger(final Class<?> clazz)
	{
		return getLogger(clazz.getSimpleName());
	}

	public Logger getLogger(final String name)
	{
		synchronized (this.cache)
		{
			Logger logger = this.cache.get(name);
			if (logger != null)
				return logger;

			// create new logger
			logger = Logger.getLogger(name);

			this.cache.put(name, logger);
			logger.setUseParentHandlers(false);

			logger.setLevel(Level.ALL);

			logger.addHandler(this);
			return logger;
		}
	}

	public void setConfig(final LogServiceConfig config)
	{
		synchronized (this.listeners)
		{
			final List<LogService.Listener> oldListeners = new ArrayList<>(this.listeners);
			removeAllListeners();

			for(final LogService.Listener listener : config.getOutputs())
			{
				final int oldIndex = oldListeners.indexOf(listener);
				if (oldIndex>=0)
					addListener(oldListeners.get(oldIndex));
				else
					addListener(listener);
			}
			setLevel(config.getLevel());
		}
	}


	public LogService addListener(final LogService.Listener listener)
	{
		synchronized (this.listeners)
		{
			this.listeners.add(listener);
		}

		return this;
	}

	public void removeAllListeners()
	{
		synchronized (this.listeners)
		{
			this.listeners.clear();
		}
	}

	public boolean removeListener(final LogService.Listener listener)
	{
		synchronized (this.listeners)
		{
			return this.listeners.remove(listener);
		}
	}

	public LogService setLogRecordFormatter(final LogRecordFormatter logRecordFormatter)
	{
		synchronized (this.lock)
		{
			this.recordFormatter = logRecordFormatter;
		}

		return this;
	}

	public LogService setClock(final Clock clock)
	{
		synchronized (this.lock)
		{
			this.clock = clock;

		}

		return this;
	}


	public static interface Listener
	{
		void onEvent(final LogService.Event event);
	}

	public static class Event implements Comparable<LogService.Event>
	{
		private static final Comparator<LogService.Event> TIME_COMPARATOR = Comparator.comparing(Event::getTime);

		private final LocalDateTime time;
		private final LogRecord logRecord;
		private final String formattedMessage;

		public Event(final LocalDateTime time, final LogRecord logRecord, final String formattedMessage)
		{
			this.time = time;
			this.logRecord = logRecord;
			this.formattedMessage = formattedMessage;
		}

		@Override
		public int compareTo(final Event event)
		{
			return TIME_COMPARATOR.compare(this, event);
		}

		public LocalDateTime getTime()
		{
			return this.time;
		}

		public LogRecord getLogRecord()
		{
			return this.logRecord;
		}

		public String getFormattedMessage()
		{
			return this.formattedMessage;
		}

		public String getLoggerName()
		{
			return this.logRecord.getLoggerName();
		}

		public Level getLogLevel()
		{
			return this.logRecord.getLevel();
		}
	}

	public static interface LogRecordFormatter
	{
		public static final LogRecordFormatter ONE_LINE_FORMATTER = new LogRecordFormatter()
		{
			@Override
			public String format(final LocalDateTime now, final LogRecord logRecord)
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("[").append(logRecord.getLevel().getName()).append("]");
				sb.append(" ").append(DATETIME_FORMATTER.format(now));
				sb.append("|").append(logRecord.getLoggerName());
				sb.append("|").append(Thread.currentThread().getName());
				sb.append("|").append(FormatUtils.extractSimpleClassName(logRecord.getSourceClassName())).append(".")
				  .append(logRecord.getSourceMethodName());
				sb.append(": ").append(logRecord.getMessage());

				return sb.toString();
			}
		};

		public static final LogRecordFormatter TWO_LINE_FORMATTER = new LogRecordFormatter()
		{
			@Override
			public String format(final LocalDateTime now, final LogRecord logRecord)
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("[").append(logRecord.getLevel().getName()).append("]");
				sb.append(" ").append(DATETIME_FORMATTER.format(now));
				sb.append("|").append(logRecord.getLoggerName());
				sb.append("|").append(Thread.currentThread().getName());
				sb.append("|").append(FormatUtils.extractSimpleClassName(logRecord.getSourceClassName())).append(".")
				  .append(logRecord.getSourceMethodName());
				sb.append(":\n").append(logRecord.getMessage());

				return sb.toString();
			}
		};

		public final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

		String format(final LocalDateTime now, final LogRecord logRecord);


	}
}
