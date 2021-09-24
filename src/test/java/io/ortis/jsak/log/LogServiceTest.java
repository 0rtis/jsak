package io.ortis.jsak.log;

import org.junit.*;

import java.util.logging.Logger;

public class LogServiceTest
{
	private static final int BASE_TEST_RUNS = 1_000_000;


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

		Assert.assertSame(logger, logger2);
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
