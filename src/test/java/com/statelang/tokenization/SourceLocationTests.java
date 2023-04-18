package com.statelang.tokenization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SourceLocationTests {

	@Test
	void constructorArguments() {
		assertThrows(IllegalArgumentException.class, () -> new SourceLocation(0, 0));
		assertThrows(IllegalArgumentException.class, () -> new SourceLocation(1, 0));
		assertThrows(IllegalArgumentException.class, () -> new SourceLocation(0, 1));

		assertDoesNotThrow(() -> new SourceLocation(1, 1));
	}

	@Nested
	class ComparableTest {
		@Test
		void equals() {
			assertTrue(new SourceLocation(1, 1).compareTo(new SourceLocation(1, 1)) == 0);
		}

		@Test
		void lessThan() {
			assertTrue(new SourceLocation(1, 1).compareTo(new SourceLocation(1, 5)) < 0);
			assertTrue(new SourceLocation(1, 1).compareTo(new SourceLocation(1, 5)) != 0);

			assertTrue(new SourceLocation(1, 1).compareTo(new SourceLocation(5, 1)) < 0);
			assertTrue(new SourceLocation(1, 1).compareTo(new SourceLocation(5, 1)) != 0);
		}

		@Test
		void greaterThan() {
			assertTrue(new SourceLocation(1, 5).compareTo(new SourceLocation(1, 1)) > 0);
			assertTrue(new SourceLocation(1, 5).compareTo(new SourceLocation(1, 1)) != 0);
			assertTrue(new SourceLocation(5, 1).compareTo(new SourceLocation(1, 1)) > 0);
			assertTrue(new SourceLocation(5, 1).compareTo(new SourceLocation(1, 1)) != 0);
		}
	}

	@Nested
	class ComparisonMethodsTest {
		@Test
		void equals() {
			assertTrue(new SourceLocation(1, 1).equals(new SourceLocation(1, 1)));
		}

		@Test
		void isBefore() {
			assertTrue(new SourceLocation(1, 1).isBefore(new SourceLocation(1, 5)));
			assertTrue(new SourceLocation(1, 1).isBefore(new SourceLocation(1, 5)));
			assertTrue(new SourceLocation(1, 1).isBefore(new SourceLocation(5, 1)));
			assertTrue(new SourceLocation(1, 1).isBefore(new SourceLocation(5, 1)));
		}

		@Test
		void isBeforeOrAt() {
			assertTrue(new SourceLocation(1, 1).isBeforeOrAt(new SourceLocation(1, 5)));
			assertTrue(new SourceLocation(1, 1).isBeforeOrAt(new SourceLocation(1, 1)));
		}

		@Test
		void isAfter() {
			assertTrue(new SourceLocation(1, 5).isAfter(new SourceLocation(1, 1)));
			assertTrue(new SourceLocation(1, 5).isAfter(new SourceLocation(1, 1)));
			assertTrue(new SourceLocation(5, 1).isAfter(new SourceLocation(1, 1)));
			assertTrue(new SourceLocation(5, 1).isAfter(new SourceLocation(1, 1)));
		}

		@Test
		void isAfterOrAt() {
			assertTrue(new SourceLocation(1, 5).isAfterOrAt(new SourceLocation(1, 1)));
			assertTrue(new SourceLocation(1, 1).isAfterOrAt(new SourceLocation(1, 1)));
		}
	}

	@Test
	void movedTrough() {
		var location = new SourceLocation(1, 1);

		assertEquals(location.movedTrough("x"), new SourceLocation(1, 2));
		assertEquals(location.movedTrough("txt"), new SourceLocation(1, 4));

		assertEquals(location.movedTrough("line\n"), new SourceLocation(2, 1));
		assertEquals(location.movedTrough("\n"), new SourceLocation(2, 1));
		
		assertEquals(location.movedTrough("\n\n"), new SourceLocation(3, 1));
		assertEquals(location.movedTrough("line\n\ntxt"), new SourceLocation(3, 4));
	}
}
