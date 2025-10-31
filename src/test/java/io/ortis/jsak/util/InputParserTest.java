package io.ortis.jsak.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InputParserTest
{
	@Test
	void parseBoolean()
	{
		Assertions.assertTrue(ObjectEncoder.toBoolean("true"));
		Assertions.assertTrue(ObjectEncoder.toBoolean("True"));
		Assertions.assertTrue(ObjectEncoder.toBoolean("TRUE"));
		Assertions.assertTrue(ObjectEncoder.toBoolean("  true  "));
		Assertions.assertTrue(ObjectEncoder.toBoolean("  True  "));
		Assertions.assertTrue(ObjectEncoder.toBoolean("  TRUE  "));

		Assertions.assertFalse(ObjectEncoder.toBoolean("false"));
		Assertions.assertFalse(ObjectEncoder.toBoolean("False"));
		Assertions.assertFalse(ObjectEncoder.toBoolean("FALSE"));
		Assertions.assertFalse(ObjectEncoder.toBoolean("  false  "));
		Assertions.assertFalse(ObjectEncoder.toBoolean("  False  "));
		Assertions.assertFalse(ObjectEncoder.toBoolean("  FALSE  "));

		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("t"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("f"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("0"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("yes"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("no"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("on"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("off"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean("  "));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean(""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toBoolean(null));
	}

	@Test
	void parseLong()
	{
		Assertions.assertEquals(0, ObjectEncoder.toLong("0"));
		Assertions.assertEquals(0, ObjectEncoder.toLong("  0  "));
		Assertions.assertEquals(0, ObjectEncoder.toLong("0  "));
		Assertions.assertEquals(0, ObjectEncoder.toLong("  0"));

		Assertions.assertEquals(1, ObjectEncoder.toLong("1"));
		Assertions.assertEquals(1, ObjectEncoder.toLong("  1  "));
		Assertions.assertEquals(1, ObjectEncoder.toLong("1  "));
		Assertions.assertEquals(1, ObjectEncoder.toLong("  1"));

		Assertions.assertEquals(-1, ObjectEncoder.toLong("-1"));
		Assertions.assertEquals(-1, ObjectEncoder.toLong("  -1  "));
		Assertions.assertEquals(-1, ObjectEncoder.toLong("-1  "));
		Assertions.assertEquals(-1, ObjectEncoder.toLong("  -1"));

		Assertions.assertEquals(Long.MAX_VALUE, ObjectEncoder.toLong(Long.toString(Long.MAX_VALUE)));
		Assertions.assertEquals(Long.MIN_VALUE, ObjectEncoder.toLong(Long.toString(Long.MIN_VALUE)));

		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toLong("a"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toLong("1a"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toLong("a1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toLong("  "));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toLong(""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ObjectEncoder.toLong(null));
	}

	@Test
	void parseArray()
	{
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, ObjectEncoder.toListOfString("a,b,c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", " b", " c"}, ObjectEncoder.toListOfString("a, b, c").toArray());
		Assertions.assertArrayEquals(new String[]{"a ", " b ", " c"}, ObjectEncoder.toListOfString("a , b , c").toArray());

		Assertions.assertArrayEquals(new String[]{"a b c"}, ObjectEncoder.toListOfString("a b c").toArray());
		Assertions.assertArrayEquals(new String[]{"a  b  c"}, ObjectEncoder.toListOfString("a  b  c").toArray());
		Assertions.assertArrayEquals(new String[]{"a   b   c"}, ObjectEncoder.toListOfString("a   b   c").toArray());
		Assertions.assertArrayEquals(new String[]{"a    b    c"}, ObjectEncoder.toListOfString("a    b    c").toArray());

		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, ObjectEncoder.toListOfString("a;b;c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a", " b", " c"}, ObjectEncoder.toListOfString("a; b; c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a ", " b ", " c"}, ObjectEncoder.toListOfString("a ; b ; c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a ", "b ", " c"}, ObjectEncoder.toListOfString("a ;b ; c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a", " b ", "c"}, ObjectEncoder.toListOfString("a; b ;c", ";").toArray());

	}
}
