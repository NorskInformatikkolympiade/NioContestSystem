package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.Model;
import viewmodels.ScoreboardEntry;

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
	
	public String getFullName() {
		return firstName + " " + lastName;
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
	
	public static List<ScoreboardEntry> getScoreboard() {
		List<ScoreboardEntry> scoreboard = new ArrayList<ScoreboardEntry>();
		for (Contestant contestant : Contestant.getAll()) {
			ScoreboardEntry entry = new ScoreboardEntry();
			entry.contestantId = contestant.id;
			entry.name = contestant.getFullName();
			entry.score = contestant.getTotalScore();
			scoreboard.add(entry);
		}
		
		Collections.sort(scoreboard, new Comparator<ScoreboardEntry>() {
			public int compare(ScoreboardEntry a, ScoreboardEntry b) {
				if (a.score > b.score)
					return -1;
				else if (a.score < b.score)
					return 1;
				return a.name.compareTo(b.name);
			}
		});
		
		int position = 1;
		for (int i = 0; i < scoreboard.size(); ++i) {
			// Handle ties by assigning the same position value to contestants with equal score, and skipping over the corresponding amount of position numbers
			if (i > 0 && scoreboard.get(i).score != scoreboard.get(i - 1).score)
				position = i + 1;
			scoreboard.get(i).position = position;
		}
		
		return scoreboard;
	}
}