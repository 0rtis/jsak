package jsak;

import java.time.Duration;

public class FormatUtils
{

	public static String formatByteLength(final long bytes)
	{
		return formatByteLength(bytes, true);
	}

	/**
	 * Format byte length
	 *
	 * @param length: length in bytes
	 * @param si:     International System of Unit (if true k=1000, else K=1024)
	 * @return: byte length as {@link String}
	 */
	public static String formatByteLength(final long length, final boolean si)
	{
		final int unit = si ? 1000 : 1024;

		if(length < unit)
			return length + " B";

		final int exp = (int) (Math.log(length) / Math.log(unit));
		final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

		return String.format("%.1f %sB", length / Math.pow(unit, exp), pre);
	}


	public static String formatDuration(final Duration duration)
	{
		if(duration == null)
			return null;

		final long ms = duration.toMillis();
		if(ms >= 86400000)
			return String.format("%dd %dh%02dm%02ds%03dms", ms / 86400000, (ms % 86400000) / 3600000, (ms % 3600000) / 60000, (ms % 60000) / 1000, ms % 1000);
		else
			return String.format("%dh%02dm%02ds%03dms", ms / 3600000, (ms % 3600000) / 60000, (ms % 60000) / 1000, ms % 1000);

		/*final long s = duration.toMillis() / 1000;

		if(s >= 86400)
			return String.format("%dd %dh%02dm%02ds", s / 86400, (s % 86400) / 3600, (s % 3600) / 60, (s % 60));
		else
			return String.format("%dh%02dm%02ds", s / 3600, (s % 3600) / 60, (s % 60));*/
	}


	public static String truncateString(final String msg, final int maxLength)
	{
		if(msg == null)
			return null;

		if(msg.length() <= maxLength)
			return msg;

		if(maxLength < 4)
			return msg.substring(0, maxLength);

		return msg.substring(0, maxLength - 3).concat("...");
	}

	public static String formatException(final Throwable t)
	{
		if(t == null)
			return null;

		final Throwable cause = t.getCause();
		final String msg = cause == null ? null : formatException(cause);
		return formatException(t.getClass(), msg, t.toString(), t.getStackTrace());
	}

	private static String formatException(final Class<?> exceptionClass, final String cause, final String msg, final StackTraceElement[] exceptionStack)
	{
		final StringBuilder builder = new StringBuilder();

		if(msg == null)
			builder.append(exceptionClass.getSimpleName());
		else
			builder.append(msg);

		if(exceptionStack != null)
		{
			builder.append(System.lineSeparator());
			for(final StackTraceElement stackTraceElement : exceptionStack)
			{
				final String stackElement = stackTraceElement.toString();
				builder.append(stackElement).append(System.lineSeparator());
			}
		}

		if(cause != null)
			builder.append("Caused by ").append(cause);

		return builder.toString();
	}

	public static String formatExceptionMessage(final Throwable t)
	{
		if(t == null)
			return null;

		return t.toString();
	}

	public static String formatThreadStack(final Thread thread)
	{
		final StackTraceElement[] stack = thread.getStackTrace();
		return formatThreadStack(stack, null);
	}

	public static String formatThreadStack(final Thread thread, final int maxDepth)
	{
		if(maxDepth <= 0)
			throw new IllegalArgumentException("Max depth must be positive");

		final StackTraceElement[] stack = thread.getStackTrace();
		return formatThreadStack(stack, maxDepth);
	}

	private static String formatThreadStack(final StackTraceElement[] stack, final Integer maxDepth)
	{
		final StringBuilder sb = new StringBuilder();

		final int upperBound = maxDepth == null ? stack.length : Math.min(stack.length, 2 + maxDepth);
		for(int i = 2; i < upperBound; i++)
		{
			final StackTraceElement ste = stack[i];

			sb.insert(0, ste.getLineNumber()).insert(0, ":").insert(0, ste.getMethodName()).insert(0, ".").insert(0, extractSimpleClassName(ste.getClassName()));

			if(i + 1 < upperBound)
				sb.insert(0, " -> ");
		}
		return sb.toString();
	}

	public static String extractSimpleClassName(final String className)
	{
		final String[] buffer = className.split("\\.");
		return buffer[buffer.length - 1];
	}
}
