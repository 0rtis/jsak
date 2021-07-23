package io.ortis.jsak.server.http.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ortis.jsak.JsonUtils;
import io.ortis.jsak.server.http.limiter.config.HTTPLimiterConfig;
import io.ortis.jsak.server.http.limiter.config.ImmutableHTTPLimiterConfig;

import java.util.*;

public class ImmutableHTTPConfig implements HTTPConfig
{
	private static final List<String> EMPTY_STRINGS = Collections.unmodifiableList(new LinkedList<>());
	private static final Map<String, String> EMPTY_INCLUDE_RESPONSE_HEADER = Collections.unmodifiableMap(new HashMap<>());

	private final String host;
	private final int port;
	private final int parallelism;
	private final Map<String, String> includeHttpResponseHeaders;
	private final List<String> passList;
	private final List<String> banList;
	private final ImmutableHTTPLimiterConfig limiterConfig;

	public ImmutableHTTPConfig(final String host, final int port, final int parallelism, final Map<String, String> includeHttpResponseHeaders,
			final List<String> passList, final List<String> banList, final ImmutableHTTPLimiterConfig limiterConfig)
	{
		this.host = host;
		if(this.host == null)
			throw new IllegalArgumentException("Host must be set");

		this.port = port;
		if(this.port <= 0)
			throw new IllegalArgumentException("Port must be greater than 0");

		this.parallelism = parallelism;
		if(this.parallelism <= 0)
			throw new IllegalArgumentException("Parallelism must be greater than 0");

		this.includeHttpResponseHeaders = includeHttpResponseHeaders == null ? EMPTY_INCLUDE_RESPONSE_HEADER : Collections.unmodifiableMap(includeHttpResponseHeaders);

		this.passList = passList == null ? EMPTY_STRINGS : Collections.unmodifiableList(passList);
		this.banList = banList == null ? EMPTY_STRINGS : Collections.unmodifiableList(banList);

		this.limiterConfig = limiterConfig;
		if(this.limiterConfig == null)
			throw new IllegalArgumentException("Limiter must be set");
	}

	@Override
	public String getHost()
	{
		return this.host;
	}

	@Override
	public int getPort()
	{
		return this.port;
	}

	@Override
	public int getParallelism()
	{
		return this.parallelism;
	}

	@Override
	public List<String> getPassList()
	{
		return this.passList;
	}

	@Override
	public List<String> getBanList()
	{
		return this.banList;
	}

	@Override
	public Map<String, String> getIncludeHttpResponseHeaders()
	{
		return this.includeHttpResponseHeaders;
	}

	@Override
	public HTTPLimiterConfig getHTTPLimiterConfig()
	{
		return this.limiterConfig;
	}

	public static ImmutableHTTPConfig of(String json) throws Exception
	{
		json = json.replace("\\", "/");

		final JsonParser jsonParser = new JsonParser();

		final JsonObject bean = jsonParser.parse(json).getAsJsonObject();


		final String host = JsonUtils.parseJsonElement(bean, "host").getAsString();
		final int port = JsonUtils.parseJsonElement(bean, "port").getAsInt();
		final int parallelism = JsonUtils.parseJsonElement(bean, "parallelism").getAsInt();

		final Map<String, String> includeResponseHeaders = new LinkedHashMap<>();
		if(bean.get("includeHttpResponseHeaders") != null)
		{
			final JsonArray includeResponseHeadersArray = bean.get("includeHttpResponseHeaders").getAsJsonArray();
			for(int i = 0; i < includeResponseHeadersArray.size(); i++)
			{
				final JsonObject jo = includeResponseHeadersArray.get(i).getAsJsonObject();
				if(jo.entrySet().size() != 1)
					throw new IllegalArgumentException("Invalid include response header at index " + i);

				final Map.Entry<String, JsonElement> entry = jo.entrySet().iterator().next();

				if(includeResponseHeaders.containsKey(entry.getKey()))
					throw new IllegalArgumentException("Duplicate include response header " + entry.getKey());

				includeResponseHeaders.put(entry.getKey(), entry.getValue().getAsString());
			}
		}

		final List<String> passList = new ArrayList<>();
		final List<String> banList = new ArrayList<>();
		{
			JsonElement je = bean.get("passList");
			final JsonArray passListArray = je == null || je.isJsonNull() ? null : je.getAsJsonArray();

			if(passListArray != null && !passListArray.isJsonNull())
				for(int i = 0; i < passListArray.size(); i++)
					passList.add(passListArray.get(i).getAsString());

			je = bean.get("banList");
			final JsonArray banListArray = je == null || je.isJsonNull() ? null : je.getAsJsonArray();

			if(banListArray != null && !banListArray.isJsonNull())
				for(int i = 0; i < banListArray.size(); i++)
					banList.add(banListArray.get(i).getAsString());
		}

		final ImmutableHTTPLimiterConfig limiterConfig = ImmutableHTTPLimiterConfig.of(bean.get("limiter").toString());
		return new ImmutableHTTPConfig(host, port, parallelism, includeResponseHeaders, passList, banList, limiterConfig);
	}

	private static class StringPair
	{
		private String key;
		private String value;

		public String getKey()
		{
			return this.key;
		}

		public String getValue()
		{
			return this.value;
		}
	}
}
