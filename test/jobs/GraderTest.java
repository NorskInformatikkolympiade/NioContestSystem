package jobs;

import java.util.Date;

import models.Contestant;
import models.Language;
import models.Submission;
import models.SubmissionStatus;
import models.Task;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utilities.compilers.ICompilers;

import static org.mockito.Mockito.*;

public class GraderTest extends UnitTest {
	private Grader grader;
	private ICompilers compilersMock;
	private Contestant contestant;
	private Task task;
	
	static {
		Grader.disableGradingDuringTesting = true;
	}
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		compilersMock = mock(ICompilers.class);
		grader = new Grader(compilersMock);
		contestant = new Contestant("Ola", "Nordmann", true, false).save();
		task = new Task(1, "Heisaturen", 100, "").save();
	}
	
	@Test
	public void shouldInvokeCompilerWithDataFromSubmission() {
		final Submission submission = new Submission(contestant, task, "int main() { return 0; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0).save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.OK, "", "", 1000));
		flushAndCommit();
		
		grader.grade(submission);
		
		verify(compilersMock).compile(eq(submission.language), eq(submission.sourceCode), anyString(), anyString());
	}
	
	private void flushAndCommit() {
		JPA.em().flush();
		JPA.em().getTransaction().commit();
	}
}
