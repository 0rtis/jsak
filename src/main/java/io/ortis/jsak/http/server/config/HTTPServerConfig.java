package io.ortis.jsak.http.server.config;

import io.ortis.jsak.http.server.limiter.config.HTTPLimiterConfig;

import java.util.List;
import java.util.Map;

public interface HTTPServerConfig
{
	String getHost();

	int getPort();

	int getParallelism();

	int getBacklog();

	List<String> getPassList();

	List<String> getBanList();

	Map<String, String> getIncludeHttpResponseHeaders();

	HTTPLimiterConfig getHTTPLimiterConfig();
}
