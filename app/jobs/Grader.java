package jobs;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import models.DataSet;
import models.Language;
import models.Run;
import models.RunResult;
import models.Submission;
import models.SubmissionStatus;
import models.cmd.CommandLineResult;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

import org.joda.time.DateTime;

import play.db.jpa.JPA;
import play.jobs.Job;
import utilities.cmd.CommandLineExecutor;
import utilities.cmd.FileHelper;
import utilities.cmd.ICommandLineExecutor;
import utilities.cmd.IFileHelper;
import utilities.compilers.Compilers;
import utilities.compilers.ICompilers;
import utilities.grading.IOutputComparator;
import utilities.grading.OutputComparator;

public class Grader extends Job implements Runnable {
	private static final Grader instance = new Grader();
	private final ICompilers compilers;
	private final ICommandLineExecutor commandLineExecutor;
	private final IFileHelper fileHelper;
	private final IOutputComparator outputComparator;
	private boolean shouldStop;
	private boolean isRunning;
	static boolean disableGradingDuringTesting;
	private final int EXCEPTION_EXIT_CODE = -43;
	
	private Grader() {
		this(Compilers.instance(), new CommandLineExecutor(), new FileHelper(), new OutputComparator());
	}
	
	// For testing only
	Grader(ICompilers compilers, ICommandLineExecutor commandLineExecutor, IFileHelper fileReader, IOutputComparator outputComparator) {
		this.compilers = compilers;
		this.commandLineExecutor = commandLineExecutor;
		this.fileHelper = fileReader;
		this.outputComparator = outputComparator;
	}
	
	public static Grader getInstance() {
		return instance;
	}
	
	public void doJob() {
		if (disableGradingDuringTesting)
			return;
		System.out.println("Starting grader");
		JPA.em().getTransaction().rollback(); // Abort the transaction Play! has started for the job; we'll control our transactions ourselves
		int count = 0;
		isRunning = true;
		shouldStop = false;
		while (!shouldStop) {
			try {
				while (!shouldStop) {
					Submission submission = getNextQueuedSubmission();
					if (submission == null)
						break;
					grade(submission);
				}
				if (++count % 100 == 0)
					System.out.println("Grader is alive (" + new DateTime().toString("hh:mm:ss") + ")");
				Thread.sleep(1000);
			}
			catch (Exception e) {
				System.out.println("Exception thrown while grading: " + e);
				e.printStackTrace();
			}
		}
		isRunning = false;
		shouldStop = false;
	}
	
	private Submission getNextQueuedSubmission() {
		return Submission.find("status = ? order by submittedAt asc", SubmissionStatus.QUEUED).first();
	}
	
	// Default visibility for testing
	void grade(Submission submission) {
		System.out.println("Grading the following submission:\n" +
				"  User:         " + submission.contestant.getFullName() + "\n" +
				"  Task:         " + submission.task.title + "\n" +
				"  Submitted at: " + submission.submittedAt);
		
		try {
			JPA.em().getTransaction().begin();
			submission.status = SubmissionStatus.RUNNING;
			submission.save();
			JPA.em().flush();
			JPA.em().getTransaction().commit();
		}
		catch (Exception e) {
			System.out.println("Error occurred when trying to set task status to " + SubmissionStatus.RUNNING + ": " + e);
			e.printStackTrace();
			JPA.em().getTransaction().rollback();
			return;
		}
		
		//TODO: Can we customize paths for each developer?
		String workingDir = "E:\\Private\\eclipse-workspace\\NioContestSystem\\work";
		CompileResult compileResult = compilers.compile(submission.language, submission.sourceCode, workingDir, submission.fileName);
		
		if (compileResult.status == CompileStatus.OK) {
			System.out.println("Compilation done in " + compileResult.duration + " ms. Result: " + compileResult.status);
			submission.score = runCompiledProgramOnDataSets(compileResult.executableFileName, submission);
			submission.status = SubmissionStatus.COMPLETED;
		}
		else {
			System.out.println("Compilation failed: " + compileResult.status);
			submission.status = SubmissionStatus.COMPILATION_FAILED;
			submission.compilationErrors = compileResult.stdErr;
		}
		
		try {
			JPA.em().getTransaction().begin();
			submission.save();
			JPA.em().flush();
			JPA.em().getTransaction().commit();
		}
		catch (Exception e) {
			System.out.println("Error occurred when trying to set task status to " + SubmissionStatus.COMPLETED + ": " + e);
			e.printStackTrace();
			JPA.em().getTransaction().rollback();
			return;
		}
		
		System.out.println("Done grading the following submission:\n" +
				"  User:         " + submission.contestant.getFullName() + "\n" +
				"  Task:         " + submission.task.title + "\n" +
				"  Submitted at: " + submission.submittedAt + "\n" + 
				"  Score:        " + submission.score);
	}
	
	private int runCompiledProgramOnDataSets(String programPath, Submission submission) {
		System.out.println("Running compiled program...");
		int weightSum = 0, successfulWeightSum = 0;
		for (DataSet dataSet : submission.task.dataSets) {
			weightSum += dataSet.weight;
			System.out.print("  Set #" + dataSet.number + " (" + dataSet.weight + ")... ");
			RunResult result = runCompiledProgramOnDataSet(programPath, dataSet, submission.language);
			if (result == RunResult.OK)
				successfulWeightSum += dataSet.weight;
			
			try {
				JPA.em().getTransaction().begin();
				new Run(submission, dataSet, result).save();
				JPA.em().flush();
				JPA.em().getTransaction().commit();
			}
			catch (Exception e) {
				System.out.println("Error occurred when trying to save run for data set #" + dataSet.number);
				e.printStackTrace();
				JPA.em().getTransaction().rollback();
				return 0;
			}
			
			System.out.println(result.getName());
		}
		if (weightSum == 0)
			return 0;
		int score = submission.task.weight * successfulWeightSum / weightSum;
		System.out.println("  Score: " + score + " (of " + submission.task.weight + ")");
		return score;
	}
	
	private RunResult runCompiledProgramOnDataSet(String programPath, DataSet dataSet, Language language) {
		try {
			byte[] inputData = fileHelper.readAllBytes(dataSet.getInputFileName());
			CommandLineResult runResult = commandLineExecutor.execute(
					new String[] {
						"e:\\Private\\eclipse-workspace\\NioContestSystem\\ProperRunAs\\bin\\Release\\ProperRunAs.exe", 
						"e:\\Private\\eclipse-workspace\\NioContestSystem\\work", 
						language.getRunCommand(), 
						programPath, 
						dataSet.task.timeout + ""}, 
					inputData, true, true, dataSet.task.timeout + 3000); // Allow some extra time for ProperRunAs itself (ProperRunAs will enforce the actual time limit on the contestant's program)
			if (runResult.exitCode != 0) {
				if (runResult.exitCode == EXCEPTION_EXIT_CODE) {
					System.out.println("Exception in ProperRunAs: " + runResult.stdOut);
					return RunResult.SYSTEM_ERROR;
				}
				return RunResult.RUNTIME_ERROR;
			}
			String expectedOutput = fileHelper.readAllAsString(dataSet.getOutputFileName());
			if (runResult.stdOut != null && outputComparator.compare(expectedOutput, runResult.stdOut))
				return RunResult.OK;
			else
				return RunResult.WRONG_ANSWER;
		}
		catch (TimeoutException e) {
			return RunResult.TIMEOUT;
		}
		catch (IOException e) {
			e.printStackTrace();
			return RunResult.SYSTEM_ERROR;
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			return RunResult.SYSTEM_ERROR;
		}
	}
	
	public void stop() {
		System.out.println("Shutting down grader; waiting for at most 30 seconds...");
		shouldStop = true;
		long start = System.currentTimeMillis();
		while (isRunning) {
			if (System.currentTimeMillis() - start > 30000) {
				System.out.println("Grader failed to shut down in 30 seconds");
				return;
			}
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
		}
		System.out.println("Grader shutdown complete");
	}
}
