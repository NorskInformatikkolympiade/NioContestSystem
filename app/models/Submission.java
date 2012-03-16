package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Submission extends Model {
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
}
