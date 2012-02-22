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
import models.Submission;
import models.SubmissionStatus;
import models.Task;
import play.mvc.Controller;
import viewmodels.ScoreboardEntry;

public class Submissions extends Controller {
	public static void submit() {
		render();
	}
	
	public static void handleSubmission(File sourceCodeFile) {
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(sourceCodeFile);
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			String sourceCode = Charset.forName("UTF-8").decode(bb).toString();
			Contestant contestant = Contestant.find("lastName = 'Eldhuset'").first();
			Task task = Task.find("title = 'Heisaturen'").first();
			new Submission(contestant, task, sourceCode, Language.CPP, new Date(), SubmissionStatus.QUEUED, 0).save();
		}
		catch (IOException e) {}
		finally {
			try {
				if (stream != null)
					stream.close();
			}
			catch (IOException e) {}
		}
		redirect("Tasks.index");
	}
}
