package models;

public enum SubmissionStatus {
	QUEUED("Venter"),
	RUNNING("Kjører"),
	COMPLETED("Ferdig"),
	COMPILATION_FAILED("Kompileringsfeil");
	
	private String name;
	
	private SubmissionStatus(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
