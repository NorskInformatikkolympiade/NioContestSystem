package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class User extends Model {
	public String firstName;
	public String lastName;
	public boolean isParticipant;
	public boolean isAdmin;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	public List<Submission> submissions = new ArrayList<Submission>();
	
	public User(String firstName, String lastName, boolean isParticipant, boolean isAdmin) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.isParticipant = isParticipant;
		this.isAdmin = isAdmin;
	}
	
	public static List<User> getAll() {
		return User.findAll();
	}
	
	public int getScoreForTask(Long taskId) {
		List<Submission> submissions = Submission.find("user.id = ? and task.id = ?", id, taskId).fetch();
		int maxScore = 0;
		for (Submission submission : submissions) {
			if (submission.score > maxScore)
				maxScore = submission.score;
		}
		return maxScore;
	}
}
