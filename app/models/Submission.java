package models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Submission extends Model implements Comparable<Submission> {
	@ManyToOne(optional = false)
	public Contestant contestant;
	
	@ManyToOne(optional = false)
	public Task task;
	
	@Column(columnDefinition = "text not null")
	public String sourceCode;
	
	@Column(nullable = false)
	public Language language;
	
	@Column(nullable = false)
	public Date submittedAt;
	
	@Column(nullable = false)
	public SubmissionStatus status;
	
	@Column(nullable = false)
	public int score;
	
	@Column(nullable = false)
	public String fileName;
	
	@Column(columnDefinition = "text null")
	public String compilationErrors;
	
	public Submission(Contestant contestant, Task task, String sourceCode, Language language, Date submittedAt, SubmissionStatus status, int score, String fileName) {
		this.contestant = contestant;
		this.task = task;
		this.sourceCode = sourceCode;
		this.language = language;
		this.submittedAt = submittedAt;
		this.status = status;
		this.score = score;
		this.fileName = fileName;
	}
	
	public static List<Submission> getAll() {
		return Submission.findAll();
	}
	
	public String[] getCompilationErrorLines() {
		if (compilationErrors == null)
			return new String[0];
		return compilationErrors.split("\n");
	}
	
	@Override
	public int compareTo(Submission other) {
		if (submittedAt.before(other.submittedAt))
			return 1;
		else if (submittedAt.after(other.submittedAt))
			return -1;
		else
			return 0;
	}
}
