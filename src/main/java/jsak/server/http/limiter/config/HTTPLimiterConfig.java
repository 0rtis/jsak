package jsak.server.http.limiter.config;

import jsak.server.http.config.HTTPConfig;

import java.time.Duration;

public interface HTTPLimiterConfig
{
	Duration getCleanSchedule();

	String getSerial();

	/**
	 * Request internal {@link HTTPLimiterConfig} from {@link HTTPConfig}.
	 * Use when {@link HTTPConfig} is mutable
	 */
	public class HTTPConfigWrapper implements HTTPLimiterConfig
	{
		private final HTTPConfig httpConfig;

		public HTTPConfigWrapper(final HTTPConfig httpConfig)
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
