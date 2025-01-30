package io.ortis.jsak.lang;

public final class Stack
{
	public static String getCallerClassName()
	{
		return getCallerClassName(2);
	}

	public static String getCallerClassName(int depth)
	{
		depth = Math.abs(depth) + 1;
		final StackTraceElement[] stackTrace = new Exception().getStackTrace();
		return stackTrace[depth].getClassName();
	}

}
