package io.ortis.jsak.log;

public interface LogOutput
{
	public static final LogOutput SYSTEM_OUTPUT_STREAM = System.out::println;

	void write(final String message);
}
