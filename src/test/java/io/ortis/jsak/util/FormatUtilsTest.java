package io.ortis.jsak.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class FormatUtilsTest
{
	@Test
	public void testF()
	{
		final LocalDateTime now = LocalDateTime.now();
		final String formatedString = io.ortis.jsak.util.FormatUtils.f("There is only {} hours each {}. Current time is {}", 24, "day", now);
		final String formatedString2 = "There is only %s hours each %s. Current time is %s".formatted(24, "day", now);
		System.out.println(formatedString);
		System.out.println(formatedString2);
		Assertions.assertEquals(formatedString, formatedString2);
	}
}
