package jsak.server.http;

import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("restriction")
public class HTTPServer
{
	private final com.sun.net.httpserver.HttpServer server;

	public HTTPServer(final InetSocketAddress inetSocketAddress, final ExecutorService pool, final int backlog) throws IOException
	{
		this.server = com.sun.net.httpserver.HttpServer.create(inetSocketAddress, backlog);
		this.server.setExecutor(pool);
	}

	public void addContext(final String path, final HttpHandler httpHandler)
	{
		this.server.createContext(path, httpHandler);
	}

	public void start()
	{
		this.server.start();
	}

	public void stop()
	{
		this.server.stop(10);
	}
}
