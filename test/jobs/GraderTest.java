package jobs;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import models.Contestant;
import models.DataSet;
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
import org.mockito.ArgumentCaptor;

import play.db.jpa.JPA;
import play.test.Fixtures;
import play.test.UnitTest;
import sun.invoke.util.VerifyAccess;
import utilities.cmd.ICommandLineExecutor;
import utilities.cmd.IFileHelper;
import utilities.compilers.ICompilers;

import static org.mockito.Mockito.*;

public class GraderTest extends UnitTest {
	private Grader grader;
	private ICompilers compilersMock;
	private ICommandLineExecutor commandLineMock;
	private IFileHelper fileHelperMock;
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
		fileHelperMock = mock(IFileHelper.class);
		grader = new Grader(compilersMock, commandLineMock, fileHelperMock);
		contestant = new Contestant("Ola", "Nordmann", true, false).save();
		task = new Task(1, "Heisaturen", 100, "C:/dataSets").save();
		new DataSet(task, 1, 10).save();
		new DataSet(task, 2, 20).save();
		new DataSet(task, 3, 40).save();
		task.refresh();
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
	public void shouldRunCompiledProgramAgainstAllTaskDataSetsAndSetStatusToCompletedWhenCompilationSucceeds() throws TimeoutException, InterruptedException, IOException {
		final Submission submission = new Submission(contestant, task, "int main() { return a; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0).save();
		byte[][] inputFiles = new byte[][] {
			new byte[]{65, 66, 67},
			new byte[]{97, 98, 99},
			new byte[]{105, 102}
		};
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.OK, "", "", 1000));
		when(fileHelperMock.readAllBytes("C:/dataSets/1.in")).thenReturn(inputFiles[0]);
		when(fileHelperMock.readAllBytes("C:/dataSets/2.in")).thenReturn(inputFiles[1]);
		when(fileHelperMock.readAllBytes("C:/dataSets/3.in")).thenReturn(inputFiles[2]);
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		ArgumentCaptor<String[]> stringArrayCaptor = ArgumentCaptor.forClass(String[].class);
		ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(commandLineMock, times(3)).execute(stringArrayCaptor.capture(), byteArrayCaptor.capture(), eq(true), eq(true), eq(1000L));
		for (int i = 0; i < 3; ++i) {
			assertArrayEquals(new String[] {"E:\\Private\\eclipse-workspace\\NioContestSystem\\work\\Program.exe"}, stringArrayCaptor.getAllValues().get(i));
			assertArrayEquals(inputFiles[i], byteArrayCaptor.getAllValues().get(i));
		}
	}
	
	private void flushAndCommit() {
		JPA.em().flush();
		JPA.em().getTransaction().commit();
	}
}
