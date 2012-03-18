package controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.Contestant;
import models.Submission;
import models.Task;
import play.mvc.Controller;
import play.mvc.With;
import viewmodels.ScoreboardEntry;

@With(Security.class)
public class Contestants extends Controller {
	public static void show(Long id) {
		Contestant contestant = Contestant.findById(id);
		if (contestant == null)
			notFound("Det finnes ingen deltager med id " + id + ".");
		if (contestant.username.equals(Security.connected()) || Security.getCurrentContestant().isAdmin) {
			Collections.sort(contestant.submissions);
			render(contestant);
		}
		else
			forbidden();
	}
	
	public static void self() {
		Contestant contestant = Security.getCurrentContestant();
		if (contestant != null) {
			Collections.sort(contestant.submissions);
			renderTemplate("Contestants/show.html", contestant);
		}
		else
			forbidden();
	}
	
	public static void scoreboard() {
		if (Security.getCurrentContestant().isAdmin) {
			List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
			List<Task> tasks = Task.getAll();
			render(scoreboard, tasks);
		}
		else
			forbidden();
	}
}
