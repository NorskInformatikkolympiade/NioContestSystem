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
import models.cmd.CommandLineResult;
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
	public void setup() throws TimeoutException, IOException, InterruptedException {
		Fixtures.deleteDatabase();
		compilersMock = mock(ICompilers.class);
		commandLineMock = mock(ICommandLineExecutor.class);
		fileHelperMock = mock(IFileHelper.class);
		when(commandLineMock.execute((String[])anyObject(), (byte[])anyObject(), anyBoolean(), anyBoolean(), anyLong())).thenReturn(new CommandLineResult(0, "", "", 100));
		grader = new Grader(compilersMock, commandLineMock, fileHelperMock);
		contestant = new Contestant("Ola", "Nordmann", false).save();
		task = new Task(1, "Heisaturen", 3, "C:/dataSets").save();
		new DataSet(task, 1, 1).save();
		new DataSet(task, 2, 1).save();
		new DataSet(task, 3, 1).save();
		task.refresh();
	}
	
	@Test
	public void shouldInvokeCompilerWithDataFromSubmission() {
		final Submission submission = new Submission(contestant, task, "int main() { return 0; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0, "main.cpp").save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.OK, "", "", 1000, "main.exe"));
		flushAndCommit();
		
		grader.grade(submission);
		
		verify(compilersMock).compile(eq(submission.language), eq(submission.sourceCode), anyString(), anyString());
	}
	
	@Test
	public void shouldSetStatusToCompletedButNotInvokeCommandLineExecutorWhenCompilationFailsBecauseOfCodeError() {
		final Submission submission = new Submission(contestant, task, "int main() { return a; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0, "main.cpp").save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.CODE_ERROR, "", "", 1000, "main.exe"));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verifyZeroInteractions(commandLineMock);
	}

	@Test
	public void shouldSetStatusToCompletedButNotInvokeCommandLineExecutorWhenCompilationFailsBecauseOfInternalError() {
		final Submission submission = new Submission(contestant, task, "int main() { return 0; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0, "main.cpp").save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.INTERNAL_ERROR, "", "", 1000, "main.exe"));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verifyZeroInteractions(commandLineMock);
	}
	
	@Test
	public void shouldSetStatusToCompletedButNotInvokeCommandLineExecutorWhenCompilationFailsBecauseOfTimeout() {
		final Submission submission = new Submission(contestant, task, "int main() { return 0; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0, "main.cpp").save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.TIMEOUT, "", "", 10000, "main.exe"));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(SubmissionStatus.COMPLETED, submission.status);
		verifyZeroInteractions(commandLineMock);
	}
	
	@Test
	public void shouldRunCompiledProgramAgainstAllTaskDataSetsAndSetStatusToCompletedWhenCompilationSucceeds() throws TimeoutException, InterruptedException, IOException {
		final Submission submission = new Submission(contestant, task, "int main() { return 0; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0, "main.cpp").save();
		byte[][] inputFiles = new byte[][] {
			new byte[]{65, 66, 67},
			new byte[]{97, 98, 99},
			new byte[]{105, 102}
		};
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.OK, "", "", 1000, "main.exe"));
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
	
	@Test
	public void shouldAssignPointsForEachDataSetWhereTheProgramReturnsSuccessfullyAndProducesTheExpectedOutput() throws TimeoutException, InterruptedException, IOException {
		final Submission submission = new Submission(contestant, task, "int main() { return 0; }", Language.C, new Date(2010, 1, 1), SubmissionStatus.QUEUED, 0, "main.cpp").save();
		when(compilersMock.compile(any(Language.class), anyString(), anyString(), anyString())).thenReturn(new CompileResult(CompileStatus.OK, "", "", 1000, "main.exe"));
		when(fileHelperMock.readAllBytes(anyString())).thenReturn(new byte[0]);
		when(fileHelperMock.readAllAsString("C:/dataSets/1.out")).thenReturn("ab\ncde fg\n");
		when(fileHelperMock.readAllAsString("C:/dataSets/2.out")).thenReturn("qw\nsd tyu\n");
		when(fileHelperMock.readAllAsString("C:/dataSets/3.out")).thenReturn("abc\n");
		when(commandLineMock.execute((String[])anyObject(), (byte[])anyObject(), anyBoolean(), anyBoolean(), anyLong())).thenReturn(
				new CommandLineResult(0, "ab\ncde fg\n", "", 100),
				new CommandLineResult(0, "qw\nnsdtyu\n", "", 100),
				new CommandLineResult(1, "abc\n", "", 100));
		flushAndCommit();
		
		grader.grade(submission);
		
		submission.refresh();
		assertEquals(1, submission.score);
	}
	
	private void flushAndCommit() {
		JPA.em().flush();
		JPA.em().getTransaction().commit();
	}
}
