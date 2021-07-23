package io.ortis.jsak.server.http.limiter;


import io.ortis.jsak.server.http.limiter.config.HTTPLimiterConfig;
import io.ortis.jsak.FormatUtils;
import io.ortis.jsak.server.http.config.HTTPConfig;

import java.time.Duration;
import java.util.logging.Logger;

public class HTTPLimiterWrapper implements HTTPLimiter, Runnable
{
	private static final Duration RESTART_DELAY = Duration.ofMinutes(1);

	private final HTTPLimiterConfig config;
	private final Logger log;

	private final Object lock = new Object();
	private String serial;
	private HTTPLimiter limiter;

	public HTTPLimiterWrapper(final HTTPConfig config, final Logger log) throws Exception
	{
		this(new HTTPLimiterConfig.HTTPConfigWrapper(config), log);
	}

	public HTTPLimiterWrapper(final HTTPLimiterConfig config, final Logger log) throws Exception
	{
		this.config = config;
		this.log = log;

		update(this.config.getSerial());
	}

	public HTTPLimiterWrapper start()
	{
		final Thread t = new Thread(this);
		t.setName(getClass().getSimpleName());
		t.start();

		return this;
	}

	@Override
	public void run()
	{
		try
		{
			long lastClean = System.currentTimeMillis();
			while(!Thread.interrupted())
			{
				try
				{

					update(this.config.getSerial());

					final long cleanSchedule = this.config.getCleanSchedule().toMillis();
					if(cleanSchedule > 0 && System.currentTimeMillis() - lastClean > cleanSchedule)
					{
						this.log.fine("Cleaning limiter");
						this.limiter.clean(System.currentTimeMillis());
						lastClean = System.currentTimeMillis();
					}


					Thread.sleep(5000);

				} catch(final InterruptedException e)
				{
					throw e;
				} catch(final Exception e)
				{
					this.log.severe("Error in " + this.getClass().getSimpleName() + " engine - " + FormatUtils.formatException(e));
					this.log.info("Waiting " + RESTART_DELAY + " before restart");

					Thread.sleep(RESTART_DELAY.toMillis());
				}
			}

		} catch(final InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch(final Exception e)
		{
			this.log.severe("Critical error in " + this.getClass().getSimpleName() + " engine - " + FormatUtils.formatException(e));

		} finally
		{
			this.log.severe("Stopped");
		}
	}

	public void update(final String serial) throws Exception
	{
		synchronized(this.lock)
		{
			if(this.serial != null && this.serial.equals(serial))
				return;

			this.log.info("Reloading limiter");

			final HTTPLimiter newLimiter = HTTPLimiter.of(serial);
			this.serial = serial;
			this.limiter = newLimiter;
		}
	}

	@Override
	public String onRequest(final String host, final long now)
	{
		return this.limiter.onRequest(host, now);
	}

	@Override
	public void clean(final long now)
	{
		this.limiter.clean(now);
	}

}
