package jobs;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import models.Contestant;
import models.Language;
import models.Submission;
import models.SubmissionStatus;
import models.Task;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import utilities.cmd.ICommandLineExecutor;
import utilities.compilers.ICompilers;

import static org.mockito.Mockito.*;

public class GraderTest extends UnitTest {
	private Grader grader;
	private ICompilers compilersMock;
	private ICommandLineExecutor commandLineMock;
	private Contestant contestant;
	private Task task;
	
	static {
		Grader.disableGradingDuringTesting = true;
	}
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		compilersMock = mock(ICompilers.class);
		commandLineMock = mock(ICommandLineExecutor.class);
		grader = new Grader(compilersMock, commandLineMock);
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
	
	@Test
	public void shouldSetStatusToCompletedButNotInvokeCommandLineExecutorWhenCompilationFailsBecauseOfCodeError() {
		final Submission submission = new Submission(contestant, task, "int main() { return a; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0).save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.CODE_ERROR, "", "", 1000));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verifyZeroInteractions(commandLineMock);
	}

	@Test
	public void shouldSetStatusToCompletedButNotInvokeCommandLineExecutorWhenCompilationFailsBecauseOfInternalError() {
		final Submission submission = new Submission(contestant, task, "int main() { return a; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0).save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.INTERNAL_ERROR, "", "", 1000));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verifyZeroInteractions(commandLineMock);
	}
	
	@Test
	public void shouldSetStatusToCompletedButNotInvokeCommandLineExecutorWhenCompilationFailsBecauseOfTimeout() {
		final Submission submission = new Submission(contestant, task, "int main() { return a; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0).save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.TIMEOUT, "", "", 10000));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verifyZeroInteractions(commandLineMock);
	}
	
	@Test
	public void shouldRunCompiledProgramWhenCompilationSucceedsAndSetStatusToCompleted() throws TimeoutException, InterruptedException, IOException {
		final Submission submission = new Submission(contestant, task, "int main() { return a; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0).save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.OK, "", "", 1000));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verify(commandLineMock).execute(argThat(new ArrayMatcher(new String[]{"E:\\Private\\eclipse-workspace\\NioContestSystem\\work\\Program.exe"})), eq(true), eq(true), eq(1000L));
	}
	
	private void flushAndCommit() {
		JPA.em().flush();
		JPA.em().getTransaction().commit();
	}
	
	private class ArrayMatcher extends BaseMatcher<String[]> {
		private final String[] expected;
		
		public ArrayMatcher(final String[] expected) {
			this.expected = expected;
		}
		
		@Override
		public boolean matches(Object item) {
			if (!(item instanceof String[]))
				return false;
			String[] actual = (String[])item;
			if (actual.length != expected.length)
				return false;
			for (int i = 0; i < actual.length; ++i)
				if (!(actual[i] == null && expected[i] == null || actual[i].equals(expected[i])))
					return false;
			return true;
		}
		
		@Override
		public void describeTo(Description description) {
		}
	}
}
