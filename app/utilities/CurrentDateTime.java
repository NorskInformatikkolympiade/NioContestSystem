package utilities;

import java.util.Date;

public class CurrentDateTime {
	private static Date mockDateTime;
	
	public static Date get() {
		if (mockDateTime == null)
			return new Date();
		return mockDateTime;
	}
	
	public static void mock(Date mockDateTime) {
		CurrentDateTime.mockDateTime = mockDateTime;
	}
	
	public static void unmock() {
		CurrentDateTime.mockDateTime = null;
	}
}
