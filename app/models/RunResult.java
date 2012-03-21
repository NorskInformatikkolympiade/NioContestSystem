package models;

public enum RunResult {
	OK("Ok"),
	WRONG_ANSWER("Feil svar"),
	RUNTIME_ERROR("Kjøretidsfeil"),
	TIMEOUT("Timeout"),
	SYSTEM_ERROR("Systemfeil - kontakt arrangørene");
	
	private String name;
	
	private RunResult(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
