package io.ortis.jsak.http.server.limiter.config;

import io.ortis.jsak.http.server.config.HTTPServerConfig;

import java.time.Duration;

public interface HTTPLimiterConfig
{
	Duration getCleanSchedule();

	String getSerial();

	/**
	 * Request internal {@link HTTPLimiterConfig} from {@link HTTPServerConfig}.
	 * Use when {@link HTTPServerConfig} is mutable
	 */
	public class HTTPConfigWrapper implements HTTPLimiterConfig
	{
		private final HTTPServerConfig httpConfig;

		public HTTPConfigWrapper(final HTTPServerConfig httpConfig)
		{
			this.httpConfig = httpConfig;
		}

		@Override
		public Duration getCleanSchedule()
		{
			return this.httpConfig.getHTTPLimiterConfig().getCleanSchedule();
		}

		@Override
		public String getSerial()
		{
			return this.httpConfig.getHTTPLimiterConfig().getSerial();
		}
	}
}
