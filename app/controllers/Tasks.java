package controllers;

import java.util.List;

import models.Submission;
import models.Task;
import play.mvc.Controller;

public class Tasks extends Controller {
	public static void index() {
		List<Task> tasks = Task.getAll();
		render(tasks);
	}
	
	public static void show(Long id) {
		Task task = Task.findById(id);
		render(task);
	}
}
