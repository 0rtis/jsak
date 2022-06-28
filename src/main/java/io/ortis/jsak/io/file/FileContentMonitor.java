package io.ortis.jsak.io.file;


import io.ortis.jsak.FormatUtils;
import io.ortis.jsak.io.bytes.Bytes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

/**
 * Monitor the content of files. All files are read and loaded in RAM. Should only be use for small size files.
 * For large file, use {@link WatchService} from NIO package.
 */
public class FileContentMonitor implements Runnable
{
	public static final Duration COOLDOWN = Duration.ofSeconds(60);

	private final Duration pulse;
	private final long pulseMillis;
	private final Logger logger;

	private final List<FileContentListener> listeners = new LinkedList<>();
	private final Object lock = new Object();
	private final Map<Path, Bytes> files;

	public FileContentMonitor(final Path filePath, final Duration pulse) throws IOException
	{
		this(filePath, pulse, null);
	}

	public FileContentMonitor(final Path filePath, final Duration pulse, final Logger logger) throws IOException
	{
		this(Collections.singletonList(filePath), pulse, logger);
	}

	public FileContentMonitor(final List<Path> filePaths, final Duration pulse) throws IOException
	{
		this(filePaths, pulse, null);
	}

	public FileContentMonitor(final List<Path> filePaths, final Duration pulse, final Logger logger) throws IOException
	{
		this.files = new LinkedHashMap<>();
		for (final Path path : filePaths)
		{
			if (!this.files.containsKey(path))
				this.files.put(path, Bytes.wrap(Files.readAllBytes(path)));
		}

		this.pulse = pulse;
		this.pulseMillis = this.pulse.toMillis();
		if (this.pulseMillis < 0)
			throw new IllegalArgumentException("Invalid pulse duration");

		this.logger = logger;
	}

	@Override
	public void run()
	{
		try
		{
			while (!Thread.interrupted())
			{
				try
				{
					final long start = System.currentTimeMillis();

					final List<Path> paths = new LinkedList<>(this.files.keySet());
					for (final Path path : paths)
					{
						final Bytes previousFileSerial = this.files.get(path);
						final Bytes newFileSerial = Bytes.wrap(Files.readAllBytes(path));
						if (!newFileSerial.equals(previousFileSerial))
						{
							if (this.logger != null)
								this.logger.info("File '" + path + "' has changed");

							synchronized (this.lock)
							{
								this.files.put(path, newFileSerial);
							}

							synchronized (this.listeners)
							{
								for (final FileContentListener fl : this.listeners)
									fl.onFileContentChange(path, newFileSerial);
							}
						}
					}

					final long end = System.currentTimeMillis();
					final long sleep = this.pulseMillis - (end - start);
					if (sleep < 0)
					{
						if (this.logger != null)
							this.logger.warning("Monitoring loop is late by " + FormatUtils.formatDuration(Duration.ofMillis(sleep).negated()));
					} else
						Thread.sleep(sleep);


				} catch (final InterruptedException e)
				{
					throw e;
				} catch (final Exception e)
				{
					if (this.logger != null)
					{
						this.logger.severe("Error while monitoring files\n" + FormatUtils.formatException(e));
						this.logger.info(FormatUtils.formatDuration(COOLDOWN) + " before resuming files monitoring");
					} else
						e.printStackTrace();

					Thread.sleep(COOLDOWN.toMillis());
				}
			}

		} catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch (final Exception e)
		{
			if (this.logger != null)
				this.logger.severe("Critical error while monitoring files\n" + FormatUtils.formatException(e));
			else
				e.printStackTrace();
		}
	}

	public Bytes getFileContent(final Path path)
	{
		synchronized (this.lock)
		{
			return this.files.get(path);
		}
	}

	public boolean addListener(final FileContentListener fileListener)
	{
		synchronized (this.listeners)
		{
			return this.listeners.add(fileListener);
		}
	}
}
