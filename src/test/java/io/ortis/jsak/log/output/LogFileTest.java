package io.ortis.jsak.log.output;

import io.ortis.jsak.TestUtils;
import io.ortis.jsak.log.LogService;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFileTest
{
	private static Path workspace;
	private Path logFilePath = null;


	@BeforeAll
	static void beforeAll() throws Exception
	{
		workspace = TestUtils.mkdir();

	}

	@AfterAll
	static void afterAll() throws Exception
	{
		TestUtils.delete(workspace);
	}

	@BeforeEach
	void setUp()
	{
		logFilePath = workspace.resolve(TestUtils.randomString(16) + ".log");
	}

	@Test
	public void testWrite() throws Exception
	{
		final LogFile logFile = new LogFile(this.logFilePath, 100L, ChronoUnit.MINUTES);

		Assertions.assertFalse(Files.exists(this.logFilePath));

		logFile.onEvent(new LogService.Event(LocalDateTime.now(), new LogRecord(Level.INFO, "Hello world !"), "Hello world !"));

		Assertions.assertTrue(Files.exists(this.logFilePath));
		Assertions.assertEquals("Hello world !\n", Files.readString(this.logFilePath));
	}

	@Test
	public void testMaxSize() throws Exception
	{
		final long maxSize = 50;
		final LogFile logFile = new LogFile(this.logFilePath, maxSize, null);

		final long fileCount = Files.walk(workspace).count();

		while (Files.exists(this.logFilePath) && Files.size(this.logFilePath) < maxSize)
		{
			logFile.onEvent(new LogService.Event(LocalDateTime.now(), new LogRecord(Level.INFO, "Hello world !"), "Hello world !"));
		}

		logFile.onEvent(new LogService.Event(LocalDateTime.now(), new LogRecord(Level.INFO, "Hello world !"), "Hello world !"));
		Assertions.assertEquals(fileCount + 1, Files.walk(workspace).count());
	}

	@Test
	public void testRotation() throws Exception
	{
		try
		{
			new LogFile(this.logFilePath, null, ChronoUnit.SECONDS);
			Assertions.fail("Should not allow invalid rotation");
		} catch (final Exception ignored)
		{
		}

		final LogFile logFile = new LogFile(this.logFilePath, null, ChronoUnit.MINUTES);
		logFile.onEvent(new LogService.Event(LocalDateTime.now(), new LogRecord(Level.INFO, "Hello world !"), "Hello world !"));

		final long fileCount = Files.walk(workspace).count();

		Thread.sleep(ChronoUnit.MINUTES.getDuration().plusSeconds(1).toMillis());
		logFile.onEvent(new LogService.Event(LocalDateTime.now(), new LogRecord(Level.INFO, "Hello world !"), "Hello world !"));
		Assertions.assertEquals(fileCount + 1, Files.walk(workspace).count());
	}


}
