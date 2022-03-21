package io.ortis.jsak.http.server.limiter.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ortis.jsak.JsonUtils;

import java.time.Duration;

public class ImmutableHTTPLimiterConfig implements HTTPLimiterConfig
{
	private final Duration cleanSchedule;
	private final String serial;

	public ImmutableHTTPLimiterConfig(final Duration cleanSchedule, final String serial)
	{
		this.cleanSchedule = cleanSchedule;
		this.serial = serial;
	}

	@Override
	public Duration getCleanSchedule()
	{
		return this.cleanSchedule;
	}

	@Override
	public String getSerial()
	{
		return this.serial;
	}

	public static ImmutableHTTPLimiterConfig of(String json) throws Exception
	{
		json = json.replace("\\", "/");

		final JsonParser jsonParser = new JsonParser();

		final JsonObject bean = jsonParser.parse(json).getAsJsonObject();

		final Duration cleanSchedule = Duration.parse(JsonUtils.parseJsonElement(bean, "cleanSchedule").getAsString());

		return new ImmutableHTTPLimiterConfig(cleanSchedule, json);
	}
}

