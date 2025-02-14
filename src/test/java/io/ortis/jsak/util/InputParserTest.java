package io.ortis.jsak.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class InputParserTest
{
	@Test
	void parseBoolean()
	{
		Assertions.assertTrue(InputParser.parseBoolean("true"));
		Assertions.assertTrue(InputParser.parseBoolean("True"));
		Assertions.assertTrue(InputParser.parseBoolean("TRUE"));
		Assertions.assertTrue(InputParser.parseBoolean("  true  "));
		Assertions.assertTrue(InputParser.parseBoolean("  True  "));
		Assertions.assertTrue(InputParser.parseBoolean("  TRUE  "));

		Assertions.assertFalse(InputParser.parseBoolean("false"));
		Assertions.assertFalse(InputParser.parseBoolean("False"));
		Assertions.assertFalse(InputParser.parseBoolean("FALSE"));
		Assertions.assertFalse(InputParser.parseBoolean("  false  "));
		Assertions.assertFalse(InputParser.parseBoolean("  False  "));
		Assertions.assertFalse(InputParser.parseBoolean("  FALSE  "));

		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("t"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("f"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("0"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("yes"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("no"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("on"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("off"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean("  "));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean(""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseBoolean(null));
	}

	@Test
	void parseLong()
	{
		Assertions.assertEquals(0, InputParser.parseLong("0"));
		Assertions.assertEquals(0, InputParser.parseLong("  0  "));
		Assertions.assertEquals(0, InputParser.parseLong("0  "));
		Assertions.assertEquals(0, InputParser.parseLong("  0"));

		Assertions.assertEquals(1, InputParser.parseLong("1"));
		Assertions.assertEquals(1, InputParser.parseLong("  1  "));
		Assertions.assertEquals(1, InputParser.parseLong("1  "));
		Assertions.assertEquals(1, InputParser.parseLong("  1"));

		Assertions.assertEquals(-1, InputParser.parseLong("-1"));
		Assertions.assertEquals(-1, InputParser.parseLong("  -1  "));
		Assertions.assertEquals(-1, InputParser.parseLong("-1  "));
		Assertions.assertEquals(-1, InputParser.parseLong("  -1"));

		Assertions.assertEquals(Long.MAX_VALUE, InputParser.parseLong(Long.toString(Long.MAX_VALUE)));
		Assertions.assertEquals(Long.MIN_VALUE, InputParser.parseLong(Long.toString(Long.MIN_VALUE)));

		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseLong("a"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseLong("1a"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseLong("a1"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseLong("  "));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseLong(""));
		Assertions.assertThrows(IllegalArgumentException.class, () -> InputParser.parseLong(null));
	}

	@Test
	void parseArray()
	{
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a,b,c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a, b, c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a , b , c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a ,b , c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a, b ,c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a,b, c").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a,b,c").toArray());

		Assertions.assertArrayEquals(new String[]{"a b c"}, InputParser.parseArrayToList("a b c").toArray());
		Assertions.assertArrayEquals(new String[]{"a  b  c"}, InputParser.parseArrayToList("a  b  c").toArray());
		Assertions.assertArrayEquals(new String[]{"a   b   c"}, InputParser.parseArrayToList("a   b   c").toArray());
		Assertions.assertArrayEquals(new String[]{"a    b    c"}, InputParser.parseArrayToList("a    b    c").toArray());

		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a;b;c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a; b; c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a ; b ; c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a ;b ; c", ";").toArray());
		Assertions.assertArrayEquals(new String[]{"a", "b", "c"}, InputParser.parseArrayToList("a; b ;c", ";").toArray());

	}
}
