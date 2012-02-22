package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
public class Contestant extends Model {
	public String firstName;
	public String lastName;
	public boolean isParticipant;
	public boolean isAdmin;
	
	@OneToMany(mappedBy = "contestant", cascade = CascadeType.ALL)
	public List<Submission> submissions = new ArrayList<Submission>();
	
	public Contestant(String firstName, String lastName, boolean isParticipant, boolean isAdmin) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.isParticipant = isParticipant;
		this.isAdmin = isAdmin;
	}
	
	public static List<Contestant> getAll() {
		return Contestant.findAll();
	}
	
	public int getScoreForTask(Long taskId) {
		List<Submission> submissions = Submission.find("contestant.id = ? and task.id = ?", id, taskId).fetch();
		int maxScore = 0;
		for (Submission submission : submissions) {
			if (submission.score > maxScore)
				maxScore = submission.score;
		}
		return maxScore;
	}

	public int getTotalScore() {
		int totalScore = 0;
		for (Task task : Task.getAll())
			totalScore += getScoreForTask(task.id);
		return totalScore;
	}
}
