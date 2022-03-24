package io.ortis.jsak.log.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ortis.jsak.JsonUtils;
import io.ortis.jsak.log.LogService;
import io.ortis.jsak.log.output.Console;
import io.ortis.jsak.log.output.LogFile;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class ImmutableLogServiceConfig implements LogServiceConfig
{

	private final Level level;
	private final List<LogService.Listener> outputs;

	public ImmutableLogServiceConfig(final Level level, final List<LogService.Listener> outputs)
	{
		this.level = level;
		this.outputs = List.copyOf(outputs);
	}

	@Override
	public Level getLevel()
	{
		return this.level;
	}

	@Override
	public List<LogService.Listener> getOutputs()
	{
		return this.outputs;
	}

	public static ImmutableLogServiceConfig of(String json, final String key)
	{
		json = JsonUtils.sanitizeJson(json);

		final JsonObject bean;
		if (key != null)
			bean = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject(key);
		else
			bean = JsonParser.parseString(json).getAsJsonObject();

		final Level level = Level.parse(JsonUtils.parseJsonElement(bean, "level").getAsString().trim().toUpperCase(Locale.ENGLISH));

		final JsonArray rawOutputs = JsonUtils.parseJsonElement(bean, "outputs").getAsJsonArray();
		final List<LogService.Listener> outputs = new LinkedList<>();

		for (int i = 0; i < rawOutputs.size(); i++)
		{
			final JsonObject jo = rawOutputs.get(i).getAsJsonObject();
			final String type = jo.get("class").getAsString().trim().toUpperCase(Locale.ENGLISH);

			if (io.ortis.jsak.log.output.Console.class.getSimpleName().toUpperCase(Locale.ENGLISH).equals(type))
				outputs.add(new Console());
			else if (LogFile.class.getSimpleName().toUpperCase(Locale.ENGLISH).equals(type))
			{
				final Path path = Path.of(jo.get("path").getAsString());
				final Long maxSize = jo.get("maxSize").isJsonNull() ? null : jo.get("maxSize").getAsLong();
				final ChronoUnit fileRotation = ChronoUnit.valueOf(jo.get("rotation").getAsString());
				outputs.add(new LogFile(path, maxSize, fileRotation));
			} else
				throw new IllegalArgumentException("Unhandled log output type " + jo.get("class").getAsString());
		}

		return new ImmutableLogServiceConfig(level, outputs);
	}
}
