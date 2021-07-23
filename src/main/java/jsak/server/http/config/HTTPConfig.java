package jsak.server.http.config;

import jsak.server.http.limiter.config.HTTPLimiterConfig;

import java.util.List;
import java.util.Map;

public interface HTTPConfig
{
	String getHost();

	int getPort();

	int getParallelism();

	List<String> getPassList();

	List<String> getBanList();

	Map<String, String> getIncludeHttpResponseHeaders();

	HTTPLimiterConfig getHTTPLimiterConfig();
}
