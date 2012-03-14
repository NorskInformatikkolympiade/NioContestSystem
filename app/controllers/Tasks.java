package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Contestant;
import models.Submission;
import models.Task;
import play.mvc.Controller;
import play.mvc.With;

@With(Security.class)
public class Tasks extends Controller {
	public static void index() {
		List<Task> tasks = Task.getAll();
		render(tasks);
	}
	
	public static void show(Long id) {
		Task task = Task.findById(id);
		Contestant currentContestant = Security.getCurrentContestant();
		List<Submission> submissions;
		if (Security.getCurrentContestant().isAdmin)
			submissions = task.submissions;
		else {
			submissions = new ArrayList<Submission>();
			for (Submission submission : task.submissions) {
				if (submission.contestant.id == currentContestant.id)
					submissions.add(submission);
			}
		}
		render(task, submissions);
	}
}
