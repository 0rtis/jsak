package io.ortis.jsak.server.http.limiter;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ortis.jsak.JsonUtils;

import java.time.Duration;
import java.util.Locale;

public interface HTTPLimiter
{
	String onRequest(final String host, final long now);

	void clean(final long now);

	public static int parseCleanScheduleMinutes(final String serial)
	{
		final JsonParser jsonParser = new JsonParser();

		final JsonObject bean = jsonParser.parse(serial).getAsJsonObject();

		final Integer cleanScheduleMinutes = bean.get("cleanScheduleMinutes").getAsJsonObject() == null ||
											 bean.get("cleanScheduleMinutes").isJsonNull() ? null :
											 bean.get("cleanScheduleMinutes").getAsInt();
		if (cleanScheduleMinutes == null)
			throw new IllegalArgumentException("Clean schedule minutes is null");

		if (cleanScheduleMinutes <= 0)
			throw new IllegalArgumentException("Clean schedule minutes must be greater than 0");

		return cleanScheduleMinutes;
	}

	public static HTTPLimiter of(final String serial)
	{
		final JsonParser jsonParser = new JsonParser();

		final JsonObject bean = jsonParser.parse(serial).getAsJsonObject();

		final String type = bean.get("type").getAsString();

		if (type == null)
			throw new IllegalArgumentException("Type not set");

		final String uppercaseType = type.toUpperCase(Locale.ENGLISH);

		final JsonObject config = bean.get("params").getAsJsonObject();

		if (uppercaseType.equals(TimeHTTPLimiter.class.getSimpleName().toUpperCase(Locale.ENGLISH)))
		{
			final int strikes = JsonUtils.parseJsonElement(config, "strikes").getAsInt();
			final Duration timeFrame = Duration.parse(JsonUtils.parseJsonElement(config, "timeFrame").getAsString());

			return new TimeHTTPLimiter(strikes, timeFrame);
		} else if (uppercaseType.equals(NoLimitHTTPLimiter.class.getSimpleName().toUpperCase(Locale.ENGLISH)))
			return new NoLimitHTTPLimiter();

		throw new RuntimeException("Unhandled HTTP limiter type " + type);
	}
}
