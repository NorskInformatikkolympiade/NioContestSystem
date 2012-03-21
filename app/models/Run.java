package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Run extends Model {
	@ManyToOne(optional = false)
	public Submission submission;
	
	@ManyToOne(optional = false)
	public DataSet dataSet;
	
	public RunResult result;

	public Run(Submission submission, DataSet dataSet, RunResult result) {
		this.submission = submission;
		this.dataSet = dataSet;
		this.result = result;
	}
}
