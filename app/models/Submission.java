package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Submission extends Model {
	@ManyToOne(optional = false)
	public User user;
	
	@ManyToOne(optional = false)
	public Task task;
	
	@Lob
	//@Type(type = "org.hibernate.type.TextType")
	public String sourceCode;
	
	public Language language;
	public Date submittedAt;
	public SubmissionStatus status;
	public int score;
	
	public Submission(User user, Task task, String sourceCode, Language language, Date submittedAt, SubmissionStatus status, int score) {
		this.user = user;
		this.task = task;
		this.sourceCode = sourceCode;
		this.language = language;
		this.submittedAt = submittedAt;
		this.status = status;
		this.score = score;
	}
	
	public static List<Submission> getAll() {
		return Submission.findAll();
	}
}
