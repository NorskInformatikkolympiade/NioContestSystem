package controllers;

import java.util.List;

import models.Task;
import play.mvc.Controller;

public class Tasks extends Controller {
	public static void index() {
		List<Task> tasks = Task.getAll();
		render(tasks);
	}
}
