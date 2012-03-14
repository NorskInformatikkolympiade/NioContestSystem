package controllers;

import java.util.List;

import models.Contestant;
import play.mvc.Controller;
import play.mvc.With;
import viewmodels.ScoreboardEntry;

@With(Secure.class)
public class Contestants extends Controller {
	public static void show(Long id) {
		Contestant contestant = Contestant.findById(id);
		if (contestant.username.equals(Security.connected()))
			render(contestant);
		else
			forbidden();
	}
	
	public static void self() {
		Contestant contestant = Security.getCurrentContestant();
		if (contestant != null)
			renderTemplate("Contestants/show.html", contestant);
		else
			forbidden();
	}
	
	public static void scoreboard() {
		List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
		render(scoreboard);
	}
}
