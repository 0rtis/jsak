package jsak.time;

import java.time.Duration;

public class Timer
{
	private long start;

	public Timer()
	{
		reset();
	}

	public void reset()
	{
		this.start = System.nanoTime();
	}

	public Duration elapsed()
	{
		final long end = System.nanoTime();
		return Duration.ofNanos(end - this.start);
	}
}
