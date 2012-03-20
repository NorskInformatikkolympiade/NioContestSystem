package utilities.grading;

public class OutputComparator implements IOutputComparator {
	public boolean compare(String expected, String actual) {
		if (expected == null || actual == null)
			return false;
		return normalize(expected).equals(normalize(actual));
	}
	
	private static String normalize(String str) {
		StringBuilder sb = new StringBuilder();
		String[] lines = str.split("[\n\r]+");
		for (String line : lines) {
			String[] tokens = line.split("[ \t]+");
			boolean anyNonempty = false;
			for (int i = 0; i < tokens.length; ++i) {
				if (tokens[i].length() == 0)
					continue;
				anyNonempty = true;
				sb.append(tokens[i]);
				sb.append(' ');
			}
			if (anyNonempty)
				sb.setCharAt(sb.length() - 1, '\n'); // Replace last space
		}
		return sb.toString();
	}
}
