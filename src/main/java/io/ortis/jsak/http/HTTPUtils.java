package io.ortis.jsak.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class HTTPUtils
{
	public static String parametersToForm(final Map<String, String> params, final boolean encode) throws UnsupportedEncodingException
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final Map.Entry<String, String> entry : params.entrySet())
		{
			if (first)
				first = false;
			else
				sb.append("&");

			if (encode)
			{
				sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
				sb.append("=");
				sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
			} else
			{
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(entry.getValue());
			}
		}

		return sb.toString();
	}


	public static String getHeader(final String name, final HttpExchange exchange)
	{
		final List<String> values = exchange.getRequestHeaders().get(name);
		if (values == null || values.isEmpty())
			return null;

		return values.get(0);
	}

	public static Map<String, List<String>> splitQuery(final String query) throws UnsupportedEncodingException
	{
		final Map<String, List<String>> queryPairs = new LinkedHashMap<>();
		if (query == null)
			return queryPairs;

		final String[] pairs = query.split("&");
		for (String pair : pairs)
		{
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
			final List<String> values = queryPairs.computeIfAbsent(key, e -> new LinkedList<>());
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
			values.add(value);
		}
		return queryPairs;
	}

	public static String paramFirstValue(final String key, final Map<String, List<String>> params)
	{
		return paramFirstValue(key, params, false);
	}

	public static String paramFirstValue(final String key, final Map<String, List<String>> params, final boolean caseSensitive)
	{
		final String uKey = key.toUpperCase(Locale.ENGLISH);

		for (final Map.Entry<String, List<String>> param : params.entrySet())
			if (key.equals(param.getKey()) || (!caseSensitive && uKey.equals(param.getKey().toUpperCase(Locale.ENGLISH))))
			{
				if (param.getValue() == null || param.getValue().isEmpty())
					return null;
				else
					return param.getValue().get(0);
			}

		return null;
	}
}
