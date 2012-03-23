package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import models.Contestant;
import models.Language;
import models.Setting;
import models.Submission;
import models.SubmissionStatus;
import models.Task;
import play.mvc.Controller;
import play.mvc.With;
import viewmodels.ScoreboardEntry;

@With(Security.class)
public class Submissions extends Controller {
	public static void show(Long id) {
		Submission submission = Submission.findById(id);
		if (submission == null)
			notFound("Det finnes ingen innlevering med id " + id + ".");
		if (submission.contestant.username.equals(Security.connected()) || Security.getCurrentContestant().isAdmin)
			render(submission);
		else
			forbidden();
	}
	
	public static void submit() {
		if (Security.getCurrentContestant().isAdmin)
			forbidden();
		else {
			List<Task> tasks = Task.getAll();
			Language[] languages = Language.values();
			render(tasks, languages);
		}
	}
	
	public static void handleSubmission(long taskId, String language, File sourceCodeFile) {
		if (Security.getCurrentContestant().isAdmin) {
			forbidden();
			return;
		}
		if (!Setting.isContestRunning()) {
			forbidden("Konkurransen er ikke aktiv nå.");
			return;
		}
		if (!sourceCodeFile.getName().matches("^[a-zA-Z0-9_.]+$")) {
			forbidden("Filnavnet kan bare inneholde tall, bokstaver og understrek");
			return;
		}
		
		Language lang = Language.valueOf(language);
		Task task = Task.findById(taskId);
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(sourceCodeFile);
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			String sourceCode = Charset.forName("UTF-8").decode(bb).toString();
			for (char c : sourceCode.toCharArray()) {
				if ((c < ' ' || c >= 256) && c != '\n' && c != '\r' && c != '\t') {
					error("Filen du prøver å laste opp inneholder ugyldige tegn. Er du sikker på at det er en kildekodefil?");
					return;
				}
			}
			Contestant contestant = Contestant.find("byUsername", Security.connected()).first();
			new Submission(contestant, task, sourceCode, lang, new Date(), SubmissionStatus.QUEUED, 0, sourceCodeFile.getName()).save();
		}
		catch (IOException e) {}
		finally {
			try {
				if (stream != null)
					stream.close();
			}
			catch (IOException e) {}
		}
		redirect("Tasks.show", task.id);
	}
}
