package io.ortis.jsak.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class LogServiceTest
{
	private static final int BASE_TEST_RUNS = 1_000_000;


	@Test
	public void test() throws Exception
	{
		final LogService logService = new LogService().start();
		logService.addListener(FilteredLogListener.CONSOLE_ALL);

		final Logger logger = logService.getLogger("test");
		Thread.sleep(1000);
		logger.severe("severe");
		Thread.sleep(1000);
		logger.warning("warning");
		Thread.sleep(1000);
		logger.info("info");
		Thread.sleep(1000);
		logger.fine("fine");
		Thread.sleep(1000);
		logger.finer("finer");
		Thread.sleep(1000);
		logger.finest("finest");
		Thread.sleep(1000);

		final Logger logger2 = logService.getLogger("test");

		Assertions.assertSame(logger, logger2);
	}

}
