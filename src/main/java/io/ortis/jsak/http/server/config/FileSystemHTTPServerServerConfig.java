package io.ortis.jsak.http.server.config;


import io.ortis.jsak.io.bytes.Bytes;
import io.ortis.jsak.io.file.FileContentListener;
import io.ortis.jsak.io.file.FileContentMonitor;
import io.ortis.jsak.http.server.limiter.config.HTTPLimiterConfig;
import io.ortis.jsak.FormatUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FileSystemHTTPServerServerConfig implements HTTPServerConfig, FileContentListener
{
	private final Path configPath;
	private final Logger log;

	private final FileContentMonitor fileContentMonitor;
	private final Object lock = new Object();
	private HTTPServerConfig httpConfig;

	public FileSystemHTTPServerServerConfig(final Path configPath, final Duration pulse, final Logger log) throws NoSuchAlgorithmException, IOException, Exception
	{
		this.configPath = configPath;
		this.fileContentMonitor = new FileContentMonitor(this.configPath, pulse, log);
		this.fileContentMonitor.addFileListener(this);
		this.log = log;

		final Bytes serial = this.fileContentMonitor.getFileContent(this.configPath);
		final String json = new String(serial.toByteArray(), StandardCharsets.UTF_8);

		this.httpConfig = ImmutableHTTPServerConfig.of(json);
	}

	public FileSystemHTTPServerServerConfig monitor()
	{
		final Thread t = new Thread(this.fileContentMonitor);
		t.setName(this.fileContentMonitor.getClass().getSimpleName());
		t.start();
		return this;
	}

	@Override
	public void onFileContentChange(final Path filePath, final Bytes serial)
	{
		if (!filePath.equals(this.configPath))
			return;

		this.log.info("Reloading config file " + this.configPath);
		final String json = new String(serial.toByteArray(), StandardCharsets.UTF_8);

		try
		{
			final HTTPServerConfig newHttpConfig = ImmutableHTTPServerConfig.of(json);

			synchronized (this.lock)
			{
				this.httpConfig = newHttpConfig;
			}

		} catch (final Exception e)
		{
			this.log.severe("Error while parsing http config - " + FormatUtils.formatException(e));
		}
	}

	@Override
	public String getHost()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getHost();
		}
	}

	@Override
	public int getPort()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getPort();
		}
	}

	@Override
	public int getParallelism()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getParallelism();
		}
	}

	@Override
	public List<String> getPassList()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getPassList();
		}
	}

	@Override
	public List<String> getBanList()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getBanList();
		}
	}

	@Override
	public Map<String, String> getIncludeHttpResponseHeaders()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getIncludeHttpResponseHeaders();
		}
	}

	@Override
	public HTTPLimiterConfig getHTTPLimiterConfig()
	{
		synchronized (this.lock)
		{
			return this.httpConfig.getHTTPLimiterConfig();
		}
	}
}
