package io.ortis.jsak.http;

import io.ortis.jsak.collection.CachedIterator;
import io.ortis.jsak.http.server.HTTPRequestHttpHandler;
import io.ortis.jsak.http.server.HTTPServer;
import io.ortis.jsak.http.server.config.HTTPServerConfig;
import io.ortis.jsak.http.server.limiter.HTTPLimiterWrapper;
import io.ortis.jsak.http.server.limiter.NoLimitHTTPLimiter;
import io.ortis.jsak.http.server.limiter.config.HTTPLimiterConfig;
import io.ortis.jsak.io.Compression;
import org.junit.*;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class HTTPServerTest
{

	private static final HTTPServerConfig CONFIG = new HTTPServerConfig()
	{
		@Override
		public String getHost()
		{
			return "0.0.0.0";
		}

		@Override
		public int getPort()
		{
			return 4242;
		}

		@Override
		public int getParallelism()
		{
			return 2;
		}

		@Override
		public int getBacklog()
		{
			return 16;
		}

		@Override
		public List<String> getPassList()
		{
			return null;
		}

		@Override
		public List<String> getBanList()
		{
			return null;
		}

		@Override
		public Map<String, String> getIncludeHttpResponseHeaders()
		{
			return null;
		}

		@Override
		public HTTPLimiterConfig getHTTPLimiterConfig()
		{
			return new HTTPLimiterConfig()
			{
				@Override
				public Duration getCleanSchedule()
				{
					return Duration.ofMinutes(10);
				}

				@Override
				public String getSerial()
				{
					return "";
				}
			};
		}
	};


	@Before
	public void setUp() throws Exception
	{

	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void test() throws Exception
	{
		final HTTPServer httpServer = new HTTPServer(new InetSocketAddress(CONFIG.getHost(), CONFIG.getPort()),
				Executors.newFixedThreadPool(CONFIG.getParallelism(), new ThreadFactory()
				{
					private final AtomicInteger id = new AtomicInteger(0);

					@Override
					public Thread newThread(final Runnable runnable)
					{
						final Thread t = new Thread(runnable);
						t.setName(HTTPServer.class.getSimpleName() + " http-" + this.id.incrementAndGet());
						return t;
					}
				}), CONFIG.getParallelism());


		httpServer.addContext("/", new HTTPRequestHttpHandler(CONFIG, List.of(), Compression.Algorithm.Raw, new NoLimitHTTPLimiter(), 4096,
				Logger.getAnonymousLogger()));

		httpServer.start();
	}


	@BeforeClass
	public static void setUpBeforeClass()
	{

	}

	@AfterClass
	public static void tearDownAfterClass()
	{

	}
}
