import java.util.Date;

import models.Contestant;
import models.Language;
import models.Submission;
import models.SubmissionStatus;
import models.Task;

public class TestData {
	public static void AddSubmissions(Contestant contestant, Task task, int ... scores) {
		for (int i = 0; i < scores.length; ++i)
			new Submission(contestant, task, "", Language.CPP, new Date(2012, 1, 1), SubmissionStatus.COMPLETED, scores[i], "main.cpp").save();
	}
}
