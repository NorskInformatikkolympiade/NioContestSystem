package models;

public enum Language {
	CPP("C++", " "),
	C("C", " "),
	JAVA("Java", "java"),
	PYTHON("Python", "python");
	
	private String name;
	private String runCommand;
	
	private Language(String name, String runCommand) {
		this.name = name;
		this.runCommand = runCommand;
	}

	public String getName() {
		return name;
	}

	public String getRunCommand() {
		return runCommand;
	}
}
