package io.ortis.jsak.log.output;

import io.ortis.jsak.log.LogService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class LogFile implements LogService.Listener
{
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmssSSS");
	private static final DecimalFormat _4_LEADING_ZEROS_FORMATTER = new DecimalFormat("0000");

	private final Path outputFilePath;
	private final Long maxFileSize;
	private final ChronoUnit fileRotation;
	private transient final Object lock = new Object();

	public LogFile(final Path outputFilePath, final Long maxFileSize, final ChronoUnit fileRotation)
	{
		this.outputFilePath = outputFilePath.toAbsolutePath();
		if (Files.isDirectory(this.outputFilePath))
			throw new IllegalArgumentException(this.outputFilePath + " is a directory");

		if (!Files.isDirectory(this.outputFilePath.getParent()))
			throw new IllegalArgumentException(this.outputFilePath + " parent path is not a directory");

		this.maxFileSize = maxFileSize != null && maxFileSize <= 0 ? null : maxFileSize;
		this.fileRotation = fileRotation;
		if (this.fileRotation != null && this.fileRotation.compareTo(ChronoUnit.MINUTES) < 0)
			throw new IllegalArgumentException("Invalid file rotation");
	}

	private void checkRotate() throws IOException
	{
		try
		{
			synchronized (this.lock)
			{
				if (!Files.exists(this.outputFilePath))
					return;

				if (this.maxFileSize != null && Files.size(this.outputFilePath) >= this.maxFileSize)
				{
					final Path archive = this.outputFilePath.getParent().resolve(nextArchiveFileName());
					Files.move(this.outputFilePath, archive, StandardCopyOption.REPLACE_EXISTING);
				} else if (this.fileRotation != null && !this.fileRotation.equals(ChronoUnit.FOREVER))
				{
					final BasicFileAttributes attr = Files.readAttributes(this.outputFilePath, BasicFileAttributes.class);
					final FileTime fileTime = attr.creationTime();
					final LocalDateTime creationTime = fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
					final LocalDateTime rotationTime = creationTime.truncatedTo(this.fileRotation).plus(this.fileRotation.getDuration());

					if (!LocalDateTime.now().isBefore(rotationTime))
					{
						final Path archive = this.outputFilePath.getParent().resolve(nextArchiveFileName());
						Files.move(this.outputFilePath, archive, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private String nextArchiveFileName()
	{
		final StringBuilder sb = new StringBuilder(this.outputFilePath.getFileName().toString());
		sb.append(".").append(DATE_TIME_FORMATTER.format(LocalDateTime.now()));

		if (Files.exists(Path.of(sb.toString())))
		{
			String suffix = null;
			for (int i = 0; i < 1000; i++)
			{
				final String _suffix = "." + _4_LEADING_ZEROS_FORMATTER.format(i);
				if (!Files.exists(Paths.get(sb + _suffix)))
				{
					suffix = _suffix;
					break;
				}
			}

			if (suffix == null)
				sb.append(".").append(UUID.randomUUID());
			else
				sb.append(suffix);
		}

		return sb.toString();
	}


	@Override
	public void onEvent(final LogService.Event event)
	{
		final String log = event.getFormattedMessage() + "\n";
		synchronized (this.lock)
		{
			try
			{
				checkRotate();
				Files.write(this.outputFilePath, log.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				checkRotate();
			} catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public int hashCode()
	{
		return this.outputFilePath.hashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final LogFile other = (LogFile) o;
		return Objects.equals(this.outputFilePath, other.outputFilePath);
	}
}
