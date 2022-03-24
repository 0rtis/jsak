package io.ortis.jsak.log.config;

import io.ortis.jsak.FormatUtils;
import io.ortis.jsak.io.bytes.Bytes;
import io.ortis.jsak.io.file.FileContentListener;
import io.ortis.jsak.io.file.FileContentMonitor;
import io.ortis.jsak.log.LogService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Logger;

public class LogConfigFileMonitor implements FileContentListener
{

	private final LogService logService;
	private final Path configPath;
	private final String configKey;
	private final Logger logger;

	private transient final FileContentMonitor fileContentMonitor;

	public LogConfigFileMonitor(final LogService logService, final Path configPath, final String configKey, final Duration pulse, final Logger logger)
			throws Exception
	{
		this.logService = logService;
		this.configPath = configPath;
		this.configKey = configKey;
		this.fileContentMonitor = new FileContentMonitor(this.configPath, pulse, logger);
		fileContentMonitor.addListener(this);
		this.logger = logger;

		final String json = new String(fileContentMonitor.getFileContent(this.configPath).toByteArray(), StandardCharsets.UTF_8);
		logService.setConfig(ImmutableLogServiceConfig.of(json, this.configKey));
	}

	public LogConfigFileMonitor start()
	{
		new Thread(this.fileContentMonitor, this.getClass().getSimpleName()).start();
		return this;
	}

	@Override
	public void onFileContentChange(final Path filePath, final Bytes serial)
	{
		if (!filePath.equals(this.configPath))
			return;

		if (this.logger != null)
			this.logger.info("Reloading config file " + this.configPath);

		final String json = new String(serial.toByteArray(), StandardCharsets.UTF_8);

		try
		{
			if (this.logger != null)
				this.logger.info("Updating log service config");
			logService.setConfig(ImmutableLogServiceConfig.of(json, this.configKey));

		} catch (final Exception e)
		{
			if (this.logger != null)
				this.logger.severe("Error while updating log service config - " + FormatUtils.formatException(e));
		}
	}

}
