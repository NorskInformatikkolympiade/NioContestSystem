package utilities.grading;
import junit.framework.TestCase;

import org.junit.Test;

import play.test.UnitTest;

public class OutputComparatorTest extends UnitTest {
	private IOutputComparator comparator = new OutputComparator();
	
	@Test
	public void shouldReturnTrueWhenGivenTwoEqualStrings() {
		assertTrue(comparator.compare("", ""));
		assertTrue(comparator.compare("Foo", "Foo"));
		assertTrue(comparator.compare("Hello\nworld!\n", "Hello\nworld!\n"));
	}
	
	@Test
	public void shouldReturnFalseWhenEitherParameterIsNull() {
		assertFalse(comparator.compare(null, null));
		assertFalse(comparator.compare(null, ""));
		assertFalse(comparator.compare("", null));
	}
	
	@Test
	public void shouldReturnFalseWhenGivenTwoDifferentStrings() {
		assertFalse(comparator.compare("Foo", "Bar"));
		assertFalse(comparator.compare("Hello", "Hallo"));
		assertFalse(comparator.compare("Foo", ""));
		assertFalse(comparator.compare("", "Bar"));
	}
	
	@Test
	public void shouldIgnoreEmptyOrBlankLines() {
		assertTrue(comparator.compare("Foo\nBar\n", "Foo\n\n\nBar\n"));
		assertTrue(comparator.compare("Foo\nBar\n", "Foo\nBar\n\n\n"));
		assertTrue(comparator.compare("Foo\n\n\nBar\n", "Foo\nBar\n"));
		assertTrue(comparator.compare("Foo\nBar\n\n\n", "Foo\nBar\n"));
		assertTrue(comparator.compare("Foo\nBar\n", "Foo\n        \t    \nBar\n"));
	}
	
	@Test
	public void shouldAllowMissingFinalNewline() {
		assertTrue(comparator.compare("Foo\nBar\n", "Foo\nBar"));
		assertTrue(comparator.compare("Foo\nBar", "Foo\nBar\n"));
		assertTrue(comparator.compare("Foo\nBar\r\n", "Foo\nBar"));
		assertTrue(comparator.compare("Foo\nBar", "Foo\nBar\r\n"));
		assertTrue(comparator.compare("Foo\nBar\r", "Foo\nBar"));
		assertTrue(comparator.compare("Foo\nBar", "Foo\nBar\r"));
	}
	
	@Test
	public void shouldHandleDifferentNewlineTypes() {
		assertTrue(comparator.compare("Hello\nWorld", "Hello\r\nWorld"));
		assertTrue(comparator.compare("Hello\nWorld", "Hello\rWorld"));
		assertTrue(comparator.compare("Hello\rWorld", "Hello\r\nWorld"));
		assertTrue(comparator.compare("Hello\rWorld", "Hello\nWorld"));
		assertTrue(comparator.compare("Hello\r\nWorld", "Hello\rWorld"));
		assertTrue(comparator.compare("Hello\r\nWorld", "Hello\nWorld"));
	}
	
	@Test
	public void shouldAllowTrailingAndLeadingWhitespace() {
		assertTrue(comparator.compare("123 456\n", "  \t  123 456\n"));
		assertTrue(comparator.compare("123 456\n", "    \t123 456 \t   \n"));
		assertTrue(comparator.compare("123 456\n", "123 456  \t    \n"));
	}
	
	@Test
	public void shouldAllowRepeatedWhitespace() {
		assertTrue(comparator.compare("Hei Hadet\n", "Hei  Hadet\n"));
		assertTrue(comparator.compare("123 456\n", "123     \t\t  \t  456\n"));
	}
	
	@Test
	public void shouldNotAllowMissingWhitespace() {
		assertFalse(comparator.compare("Hei Hadet\n", "HeiHadet\n"));
		assertFalse(comparator.compare("123 456\n", "123456\n"));
	}
	
	@Test
	public void shouldNotAllowWhitespaceWithinAToken() {
		assertFalse(comparator.compare("Heisann\n", "Hei sann\n"));
		assertFalse(comparator.compare("Heisann\n", "Hei\tsann\n"));
		assertFalse(comparator.compare("123456\n", "123 456\n"));
	}
	
	@Test
	public void shouldNotAllowNewlinesWithinALine() {
		assertFalse(comparator.compare("HeiHadet\n", "Hei\nHadet\n"));
		assertFalse(comparator.compare("Hei Hadet\n", "Hei\nHadet\n"));
	}
	
	@Test
	public void shouldNotAllowMissingNewlines() {
		assertFalse(comparator.compare("Hei\nHadet\n", "HeiHadet\n"));
		assertFalse(comparator.compare("Hei\nHadet\n", "Hei Hadet\n"));
		assertFalse(comparator.compare("Hei\nHadet\n", "Hei\tHadet\n"));
	}
}
