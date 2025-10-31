package io.ortis.jsak.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThreadsTest
{

	@Test
	void testMethod()
	{

		Assertions.assertEquals("testMethod", Threads.methodName(0));
		Assertions.assertEquals("testMethod", Threads.currentMethodName());

	}
}
