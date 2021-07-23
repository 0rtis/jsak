package jsak.server.http.limiter;

public class NoLimitHTTPLimiter implements HTTPLimiter
{
	@Override
	public String onRequest(final String host, final long now)
	{
		return null;
	}

	@Override
	public void clean(final long now)
	{

	}
}