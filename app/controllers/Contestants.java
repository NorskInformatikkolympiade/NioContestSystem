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
		render(contestant);
	}
	
	public static void scoreboard() {
		List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
		render(scoreboard);
	}
}
