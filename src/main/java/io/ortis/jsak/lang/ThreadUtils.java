package io.ortis.jsak.lang;

public abstract class ThreadUtils
{
	public static void silentSleep(final long ms)
	{
		try
		{
			Thread.sleep(ms);
		} catch(final InterruptedException ignored)
		{
		}
	}
}
